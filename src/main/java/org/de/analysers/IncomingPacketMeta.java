package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncomingPacketMeta extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 2;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        Map<ClassNode, Integer> classMap = new HashMap<>();

        for (ClassNode classNode : classes) {
            int selfFieldCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }
            }

            if (selfFieldCount > 90) {
                ArrayList<Integer> intValues = new ArrayList<>();
                for (MethodNode methodNode : classNode.methods) {
                    InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, NEW, DUP, BIPUSH);
                    if (instructionSearcher.match()) {
                        for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                            IntInsnNode intInsnNode = (IntInsnNode) abstractInsnNodes[2];
                            intValues.add(intInsnNode.operand);
                        }
                    }
                }

                // Add to map
                classMap.put(classNode, intValues.size());
            }
        }

        // Initialize variables to keep track of the maximum value and corresponding item
        int max = Integer.MIN_VALUE;
        ClassNode maxItem = null;

        // Iterate over the map to find the item with the highest integer value
        for (Map.Entry<ClassNode, Integer> entry : classMap.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                maxItem = entry.getKey();
            }
        }

        return maxItem;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, LDC, ILOAD, IMUL, PUTFIELD, -1, -1, ALOAD, LDC, ILOAD, IMUL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[4];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[11];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I") &&
                            fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("I")) {
                        addField("getId()", insnToField(fieldInsnNode, classNode));
                        addField("getLength()", insnToField(fieldInsnNode2, classNode));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

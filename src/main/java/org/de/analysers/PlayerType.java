package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class PlayerType extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 4;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (classNode.interfaces.isEmpty()) {
                continue;
            }

            int selfFieldCount = 0;
            int boolCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }

                if (fieldNode.desc.equals("Z")) {
                    boolCount++;
                }
            }

            if (selfFieldCount < 15 || boolCount != 2) {
                continue;
            }

            return classNode;
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, LDC, ILOAD, IMUL, PUTFIELD, -1, -1, ALOAD, ILOAD, LDC, IMUL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[4];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[11];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getId()", insnToField(fieldInsnNode, classNode));
                    }

                    if (fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("I")) {
                        addField("getChatBadgeId()", insnToField(fieldInsnNode2, classNode));
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, PUTFIELD, -1, -1, ALOAD, ILOAD, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[7];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                        addField("isTradable()", insnToField(fieldInsnNode, classNode));
                    }

                    if (fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("Z")) {
                        addField("isModerator()", insnToField(fieldInsnNode2, classNode));
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

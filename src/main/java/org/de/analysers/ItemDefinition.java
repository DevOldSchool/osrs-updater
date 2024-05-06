package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class ItemDefinition extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 8;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (wildcard(String.format("(L%s;*)V", classNode.name), methodNode.desc)) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }

            if (fieldNode.desc.equals("Ljava/lang/String;")) {
                addField("getName()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, GETFIELD, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[Ljava/lang/String;")) {
                        addField("getGroundActions()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_5, ANEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[Ljava/lang/String;")) {
                        addField("getInventoryActions()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD, -1, -1, ALOAD, ICONST_5);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                        addField("isMembers()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD, -1, -1, ALOAD, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                        addField("isTradable()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, BIPUSH, NEWARRAY, PUTFIELD, -1, -1, ALOAD, BIPUSH, NEWARRAY, PUTFIELD, -1, -1, ALOAD, GETFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) matches[9];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I") &&
                            fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("[I")) {
                        addField("getStackIds()", insnToField(fieldInsnNode));
                        addField("getStackAmounts()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_1, ALOAD, GETFIELD, IMUL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getPrice()", insnToField(fieldInsnNode));
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

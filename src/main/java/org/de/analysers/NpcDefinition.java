package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class NpcDefinition extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("DualNode").getNode().name)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("()V")) {
                    InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_5, ANEWARRAY, PUTFIELD, -1, -1, ALOAD, ICONST_1);
                    if (instructionSearcher.match()) {
                        for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];

                            if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[Ljava/lang/String;")) {
                                return classNode;
                            }
                        }
                    }
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
            if (fieldNode.desc.equals("[Ljava/lang/String;")) {
                addField("getActions()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IMUL, I2L);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];

                    if (fieldInsnNode.desc.equals("I") && !Modifier.isStatic(insnToField(fieldInsnNode).access)) {
                        addField("getId()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, NEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];

                    if (fieldInsnNode.desc.equals("[I")) {
                        addField("getModelIds()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }

//            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETSTATIC, PUTFIELD);
//            if (instructionSearcher.match()) {
//                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
//                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];
//
//                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Ljava/lang/String;")) {
//                        addField("getName()", insnToField(fieldInsnNode));
//                        break;
//                    }
//                }
//            }
//
//            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_5, ANEWARRAY, PUTFIELD);
//            if (instructionSearcher.match()) {
//                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
//                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];
//
//                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[Ljava/lang/String;")) {
//                        addField("getActions()", insnToField(fieldInsnNode));
//                        break;
//                    }
//                }
//            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

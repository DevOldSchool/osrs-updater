package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Skins extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 3;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("Node").getNode().name)) {
                continue;
            }

            int intCount = 0;
            int intArrayCount = 0;
            int intArrayArrayCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("I")) {
                    intCount++;
                } else if (fieldNode.desc.equals("[I")) {
                    intArrayCount++;
                } else if (fieldNode.desc.equals("[[I")) {
                    intArrayArrayCount++;
                }
            }

            if (intCount != 2 || intArrayCount != 1 || intArrayArrayCount != 1) {
                continue;
            }

            return classNode;
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<init>")) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ILOAD, LDC, IMUL, PUTFIELD);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getId()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }

                instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IMUL, ANEWARRAY, PUTFIELD);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[4];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[[I")) {
                            addField("getSkinList()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }

                instructionSearch = new InstructionSearcher(methodNode.instructions, 0, LDC, IMUL, PUTFIELD);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getCount()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class ClientPreferences extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 7;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {

            int boolCount = 0;
            int doubleCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("Z")) {
                    boolCount++;
                } else if (fieldNode.desc.equals("D")) {
                    doubleCount++;
                }
            }

            if (boolCount == 4 && doubleCount == 1) {
                return classNode;
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

            if (fieldNode.desc.startsWith("Ljava/util/")) {
                addField("getAuthTokens()", fieldNode);
            }

            if (fieldNode.desc.equals("Ljava/lang/String;")) {
                addField("getRememberedUsername()", fieldNode);
            }

            if (fieldNode.desc.equals("D")) {
                addField("getMusicVolume()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, INVOKEVIRTUAL, IMUL, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getDisplayFps()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            if (methodNode.desc.equals("(Z)V")) {
                instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, PUTFIELD);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                            addField("isRoofDisabled()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }

            if (methodNode.desc.equals("()Z")) {
                instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[1];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                            addField("isLoadingAudioDisabled()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }

            if (methodNode.name.equals("<init>")) {
                instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD, -1, -1, ALOAD, ICONST_0, PUTFIELD);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                            addField("getBrightness()", insnToField(fieldInsnNode, classNode));
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

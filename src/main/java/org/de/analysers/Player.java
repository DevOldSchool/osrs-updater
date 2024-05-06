package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Player extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("Actor").getNode().name)) {
                continue;
            }

            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("Model").getNode().name))) {
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

            if (fieldNode.desc.equals("[Ljava/lang/String;")) {
                addField("getActions()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("Model").getNode().name))) {
                addField("getModel()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("PlayerDefinition").getNode().name))) {
                addField("getPlayerDefinition()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("NameComposite").getNode().name))) {
                addField("getNameComposite()", fieldNode);
            }
        }

        InstructionSearcher instructionSearcher;
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<init>") && methodNode.desc.equals("()V")) {
                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, PUTFIELD, -1, -1, ALOAD, PUTFIELD, -1, -1, ALOAD, ICONST_3);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[1];
                        FieldInsnNode fieldInsnNode2 = (FieldInsnNode) matches[5];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I") &&
                                fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("I")) {
                            addField("getSkullIcon()", insnToField(fieldInsnNode));
                            addField("getOverheadIcon()", insnToField(fieldInsnNode2));
                            break;
                        }
                    }
                }

                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD, -1, -1, ALOAD, ICONST_0, PUTFIELD, -1, -1, ALOAD, ICONST_0, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];
                        FieldInsnNode fieldInsnNode2 = (FieldInsnNode) matches[7];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I") &&
                                fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("I")) {
                            addField("getCombatLevel()", insnToField(fieldInsnNode));
                            addField("getTotalLevel()", insnToField(fieldInsnNode2));
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

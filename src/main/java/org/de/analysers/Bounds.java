package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Bounds extends Analyser {
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
            if (classNode.superName.equals(getClassAnalyser("Node").getNode().name)) {
                continue;
            }

            int intCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                intCount++;
            }

            if (intCount != 4) {
                continue;
            }

            int initMethods = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("(II)V")) {
                    initMethods++;
                }

                if (methodNode.name.equals("<init>") && methodNode.desc.equals("(IIII)V")) {
                    initMethods++;
                }
            }

            if (initMethods != 2) {
                continue;
            }

//            System.out.println(classNode.name);
            return classNode;
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.desc.equals("(III)Z")) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ILOAD, ALOAD, GETFIELD,
                        IMUL, ILOAD, ALOAD, GETFIELD,
                        IMUL, ALOAD, GETFIELD,
                        IMUL, IADD, ILOAD, ALOAD, GETFIELD,
                        IMUL, ILOAD, ALOAD, GETFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];
                        FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[6];
                        FieldInsnNode fieldInsnNode3 = (FieldInsnNode) abstractInsnNodes[14];
                        FieldInsnNode fieldInsnNode4 = (FieldInsnNode) abstractInsnNodes[18];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getX()", insnToField(fieldInsnNode, classNode));
                        }

                        if (fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("I")) {
                            addField("getWidth()", insnToField(fieldInsnNode2, classNode));
                        }

                        if (fieldInsnNode3.owner.equals(classNode.name) && fieldInsnNode3.desc.equals("I")) {
                            addField("getY()", insnToField(fieldInsnNode3, classNode));
                        }

                        if (fieldInsnNode4.owner.equals(classNode.name) && fieldInsnNode4.desc.equals("I")) {
                            addField("getHeight()", insnToField(fieldInsnNode4, classNode));
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

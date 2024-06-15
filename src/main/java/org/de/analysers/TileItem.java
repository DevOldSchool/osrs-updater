package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class TileItem extends Analyser {
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
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("RenderableNode").getNode().name)) {
                continue;
            }

            int intCount = 0;
            int animationSequenceCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("I")) {
                    intCount++;
                }

                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("AnimationSequence").getNode().name))) {
                    animationSequenceCount++;
                }
            }

            if (intCount < 3 || animationSequenceCount > 0) {
                continue;
            }

            int modelMethods = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.desc.equals(String.format("()L%s;", getClassAnalyser("Model").getNode().name))) {
                    modelMethods++;
                }
            }

            if (modelMethods < 1) {
                continue;
            }

            return classNode;
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.desc.equals(String.format("()L%s;", getClassAnalyser("Model").getNode().name))) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IMUL, BIPUSH, INVOKESTATIC, ALOAD, GETFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];
                        FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[6];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getId()", insnToField(fieldInsnNode, classNode));
                        }

                        if (fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("I")) {
                            addField("getQuantity()", insnToField(fieldInsnNode2, classNode));
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

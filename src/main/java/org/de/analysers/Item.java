package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Item extends Analyser {
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

            int intFieldCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("I")) {
                    intFieldCount++;
                }
            }

            if (intFieldCount < 3) {
                continue;
            }

            int modelIntMethodCount = 0;
            int modelByteMethodCount = 0;
            int boolMethodCount = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.desc.equals(String.format("(I)L%s;", getClassAnalyser("Model").getNode().name))) {
                    modelIntMethodCount++;
                }

                if (methodNode.desc.equals(String.format("(B)L%s;", getClassAnalyser("Model").getNode().name))) {
                    modelByteMethodCount++;
                }

                if (methodNode.desc.equals("(I)Z")) {
                    boolMethodCount++;
                }
            }

            if (boolMethodCount < 1) {
                continue;
            }

            if (modelIntMethodCount > 0 || modelByteMethodCount > 0) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IMUL);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getId()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            if (methodNode.desc.equals(String.format("()L%s;", getClassAnalyser("Model").getNode().name))) {
                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getStackSize()", insnToField(fieldInsnNode, classNode));
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

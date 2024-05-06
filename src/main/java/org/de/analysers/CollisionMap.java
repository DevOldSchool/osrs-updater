package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class CollisionMap extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 5;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (final ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, IMUL, MULTIANEWARRAY, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] ain : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) ain[2];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[[I")) {
                            return classNode;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, IMUL, MULTIANEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];
                    if (fieldInsnNode.desc.equals("[[I")) {
                        addField("getFlags()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }

            if (methodNode.desc.equals("(IIIIZI)V")) {
                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IMUL);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[1];
                        addField("getWidthOffset()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }

                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IMUL);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[1];

                        if (fieldInsnNode.name.equals(getField("getWidthOffset()").getField().name)) {
                            continue;
                        }

                        addField("getHeightOffset()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, GETFIELD, IMUL, ALOAD, GETFIELD, IMUL);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[5];
                    addField("getHeight()", insnToField(fieldInsnNode, classNode));
                    break;
                }
            }

            if (methodNode.name.equals("<init>") && methodNode.desc.equals("(II)V")) {
                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, GETFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];
                        addField("getWidth()", insnToField(fieldInsnNode, classNode));
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

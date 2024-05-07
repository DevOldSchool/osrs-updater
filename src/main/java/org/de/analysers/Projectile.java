package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class Projectile extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 9;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 1;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("RenderableNode").getNode().name)) {
                continue;
            }

            if ((Modifier.isFinal(classNode.access))) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("<init>") && methodNode.desc.equals("(IIIIIIIIIII)V")) {
                        return classNode;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, GETFIELD, ARRAYLENGTH);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];
                    if (fieldInsnNode.owner.equals(classNode.name)) {
                        addField("getProjectileComposite()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, GETFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[0];
                    if (fieldInsnNode.desc.equals("Z") && fieldInsnNode.owner.equals(classNode.name)) {
                        addField("isMoving()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, DSUB, DLOAD, DDIV, PUTFIELD);
            if (instructionSearcher.match()) {
                for (int i = 0; i < instructionSearcher.getMatches().size(); i++) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) instructionSearcher.getMatches().get(i)[0];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) instructionSearcher.getMatches().get(i)[4];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("D") &&
                            fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("D")) {
                        switch (i) {
                            case 0:
                                addField("getX()", insnToField(fieldInsnNode, classNode));
                                addField("getSpeedX()", insnToField(fieldInsnNode2, classNode));
                                break;
                            case 1:
                                addField("getY()", insnToField(fieldInsnNode, classNode));
                                addField("getSpeedY()", insnToField(fieldInsnNode2, classNode));
                                break;
                        }
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, DSUB, ALOAD, GETFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[0];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("D") &&
                            fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("D")) {
                        addField("getZ()", insnToField(fieldInsnNode, classNode));
                        addField("getSpeedZ()", insnToField(fieldInsnNode2, classNode));
                        break;
                    }
                }
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            if (!wildcard("(*)V", methodNode.desc)) {
                continue;
            }

            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, DUP, GETFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];
                    if (fieldInsnNode.desc.equals("I") && fieldInsnNode.owner.equals(classNode.name)) {
                        addField("getDuration()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (!methodNode.desc.equals(String.format("()L%s;", getClassAnalyser("Model").getNode().name))) {
                continue;
            }

            if (Modifier.isProtected(methodNode.access)) {
                addMethod("getModel()", methodNode);
            }
        }
    }
}

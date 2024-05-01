package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class WallDecoration extends Analyser {
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
            if (!Modifier.isFinal(classNode.access)) {
                continue;
            }

            int methodCount = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (!Modifier.isStatic(methodNode.access)) {
                    methodCount++;
                }
            }

            if (methodCount > 1) {
                continue;
            }

            int intFieldCount = 0;
            int renderableNodeFieldCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("RenderableNode").getNode().name))) {
                    renderableNodeFieldCount++;
                } else if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals("I")) {
                    intFieldCount++;
                }
            }

            if (renderableNodeFieldCount != 2 && intFieldCount != 8) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("()V")) {
                    InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, INVOKESPECIAL, -1, -1, ALOAD, LCONST_0, PUTFIELD, -1, -1, ALOAD, ICONST_0, PUTFIELD, -1, -1, RETURN);
                    if (instructionSearch.match()) {
                        for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[6];

                            if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("J")) {
//                                System.out.println("FOUND CLASS " + classNode.name + " " + intFieldCount);
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
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("RenderableNode").getNode().name))) {
                addField("getRenderable()", fieldNode);
            }
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("RenderableNode").getNode().name)) && (!getField("getRenderable()").getField().name.equals(fieldNode.name))) {
                addField("getRenderable2()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, LCONST_0, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("J")) {
                        addField("getHash()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getRenderInfo()", insnToField(fieldInsnNode, classNode));
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

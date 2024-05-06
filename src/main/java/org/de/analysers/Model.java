package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.de.utilities.Pattern;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Model extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 6;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (classNode.superName.equals(getClassAnalyser("RenderableNode").getNode().name)) {
                if (!Modifier.isPublic(classNode.access)) {
                    continue;
                }

                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.desc.equalsIgnoreCase(String.format("(Z)L%s;", classNode.name))) {
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
            hookVertices(methodNode);

            if (methodNode.desc.equals("(ZZZJ)V")) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0,
                        ALOAD, GETFIELD, ILOAD, IALOAD, ISTORE, -1, -1,
                        ALOAD, GETFIELD, ILOAD, IALOAD, ISTORE, -1, -1,
                        ALOAD, GETFIELD, ILOAD, IALOAD, ISTORE, -1, -1,
                        GETSTATIC);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode yFieldInsnNode = (FieldInsnNode) matches[1];
                        FieldInsnNode zFieldInsnNode = (FieldInsnNode) matches[8];
                        FieldInsnNode xFieldInsnNode = (FieldInsnNode) matches[15];

                        if (yFieldInsnNode.owner.equals(classNode.name) && yFieldInsnNode.desc.equals("[I") &&
                                zFieldInsnNode.owner.equals(classNode.name) && zFieldInsnNode.desc.equals("[I") &&
                                xFieldInsnNode.owner.equals(classNode.name) && xFieldInsnNode.desc.equals("[I")) {
                            addField("getIndicesY()", insnToField(yFieldInsnNode, classNode));
                            addField("getIndicesZ()", insnToField(zFieldInsnNode, classNode));
                            addField("getIndicesX()", insnToField(xFieldInsnNode, classNode));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }

    private boolean hookVertices(MethodNode methodNode) {
        if (!methodNode.desc.equals("(III)V")) {
            return false;
        }

        Set<String> arrLookups = new HashSet<>();
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (abstractInsnNode.getOpcode() == GETFIELD) {
                FieldInsnNode fin = (FieldInsnNode) abstractInsnNode;
                if (fin.desc.equals("[I")) {
                    arrLookups.add(fin.name);
                }
            }
            if (abstractInsnNode.getOpcode() == IDIV) {
                return false;
            }
        }

        if (arrLookups.size() != 3) {
            return false;
        }

        AbstractInsnNode[] pat = new AbstractInsnNode[]{
                new VarInsnNode(ALOAD, 0),
                new FieldInsnNode(GETFIELD, null, null, "[I"),
                new VarInsnNode(ILOAD, 9000),
                new InsnNode(DUP2),
                new InsnNode(IALOAD),
                new VarInsnNode(ILOAD, 9000),
                new InsnNode(IADD),
                new InsnNode(IASTORE)
        };

        AbstractInsnNode start = methodNode.instructions.getFirst();
        for (int i = 0; i < 3; i++) {
            AbstractInsnNode[] ret = Pattern.findPatternSnake(methodNode, pat, start);
            if (ret != null) {
                FieldInsnNode fin = (FieldInsnNode) ret[1];
                addField(String.format("getVertices%s()", "XYZ".charAt(i)), insnToField(fin));
                start = ret[7];
            } else
                return false;
        }

        return true;
    }
}

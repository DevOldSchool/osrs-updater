package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class Varp extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                continue;
            }

            boolean hasCache = false;
            boolean hasIntArray = false;
            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, NEW, DUP, BIPUSH, INVOKESPECIAL, PUTSTATIC);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        IntInsnNode intInsnNode = (IntInsnNode) matches[2];
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[4];

                        if (intInsnNode.operand == 64 && fieldInsnNode.owner.equals(classNode.name) &&
                                fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Cache").getNode().name))) {
                            hasCache = true;
                        }
                    }
                }

                instructionSearch = new InstructionSearcher(methodNode.instructions, 0, BIPUSH, NEWARRAY, PUTSTATIC);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        IntInsnNode intInsnNode = (IntInsnNode) matches[0];
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (intInsnNode.operand == 32 && fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I")) {
                            hasIntArray = true;
                        }
                    }
                }
            }

            if (hasCache && hasIntArray) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, NEW, DUP, BIPUSH, INVOKESPECIAL, PUTSTATIC);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    IntInsnNode intInsnNode = (IntInsnNode) matches[2];
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[4];

                    if (intInsnNode.operand == 64 && fieldInsnNode.owner.equals(classNode.name) &&
                            fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Cache").getNode().name))) {
                        addField("getCache()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, LDC, INVOKEVIRTUAL, LDC, IMUL, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[6];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getConfigId()", insnToField(fieldInsnNode, classNode));
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

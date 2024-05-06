package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class ISAACCipher extends Analyser {
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
            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, SIPUSH, NEWARRAY, PUTFIELD, -1, -1, ALOAD, SIPUSH, NEWARRAY, PUTFIELD, -1, -1, ICONST_0, ISTORE);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I")) {
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
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, SIPUSH, NEWARRAY, PUTFIELD, -1, -1, ALOAD, SIPUSH, NEWARRAY, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) matches[9];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I") &&
                            fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("[I")) {
                        addField("getMemory()", insnToField(fieldInsnNode, classNode));
                        addField("getResults()", insnToField(fieldInsnNode2, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, PUTFIELD, IMUL, IADD, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getLast()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, INVOKEVIRTUAL, -1, -1, ALOAD, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[4];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getCount()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, IMUL, BIPUSH, ISHL, IXOR, IMUL, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[5];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getAccumulator()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, IADD, DUP_X1, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getCounter()", insnToField(fieldInsnNode, classNode));
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

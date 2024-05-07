package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class Animation extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 1;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        ClassNode skinsClassNode = getClassAnalyser("Skeleton").getNode();

        if (skinsClassNode == null) {
            return null;
        }

        for (ClassNode classNode : classes) {
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", skinsClassNode.name))) {
                    for (MethodNode methodNode : classNode.methods) {
                        InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, GETFIELD, ILOAD, IALOAD);
                        if (instructionSearch.match()) {
                            for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                                FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[1];

                                if (fieldInsnNode.desc.equals(String.format("L%s;", skinsClassNode.name))) {
                                    return classNode;
                                }
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
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, GETFIELD, ILOAD, IALOAD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[1];

                    if (fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Skeleton").getNode().name))) {
                        addField("getSkins()", insnToField(fieldInsnNode, classNode));
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

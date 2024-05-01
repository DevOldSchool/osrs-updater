package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class FixedSizeDequeIterator extends Analyser {
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
        ClassNode fixedSizeDeque = getClassAnalyser("FixedSizeDeque").getNode();

        if (fixedSizeDeque == null) {
            return null;
        }

        for (ClassNode classNode : classes) {
            if (classNode.interfaces.contains("java/util/Iterator")) {
                for (FieldNode fieldNode : classNode.fields) {
                    if (fieldNode.desc.equals(String.format("L%s;", fixedSizeDeque.name))) {
                        return classNode;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("FixedSizeDeque").getNode().name))) {
                addField("getDeque()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, AALOAD, GETFIELD, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) &&
                            fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Node").getNode().name))) {
                        addField("getNext()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_1, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) &&
                            fieldInsnNode.desc.equals("I")) {
                        addField("getSize()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ACONST_NULL, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) &&
                            fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Node").getNode().name))) {
                        addField("getCurrent()", insnToField(fieldInsnNode, classNode));
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

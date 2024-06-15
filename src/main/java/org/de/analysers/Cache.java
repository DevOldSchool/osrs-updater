package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class Cache extends Analyser {
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
        for (ClassNode classNode : classes) {
            if (classNode.superName.equalsIgnoreCase("java/lang/Object") &&
                    !classNode.name.equals(getClassAnalyser("Queue").getNode().name)) {
                for (FieldNode fieldNode : classNode.fields) {
                    if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("DualNode").getNode().name))) {
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
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("DualNode").getNode().name))) {
                addField("getEntityNode()", fieldNode);
            }
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("DoublyNode").getNode().name))) {
                addField("getNodeComposite()", fieldNode);
            }
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("FixedSizeDeque").getNode().name))) {
                addField("getFixedSizedDeque()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, GETFIELD, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getSize()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, ICONST_1, IADD, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getRemaining()", insnToField(fieldInsnNode, classNode));
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

package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class FixedSizeDeque extends Analyser {
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
            for (FieldNode fieldNode : classNode.fields) {
                if (!classNode.superName.equalsIgnoreCase("java/lang/object")) {
                    continue;
                }

                if (!fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("Node").getNode().name))) {
                    continue;
                }

                for (String interfaceName : classNode.interfaces) {
                    if (interfaceName.equalsIgnoreCase("java/lang/Iterable")) {
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
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, LLOAD, ALOAD, GETFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];
                    addField("getSize()", insnToField(fieldInsnNode, classNode));
                    break;
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];
                    addField("getIndex()", insnToField(fieldInsnNode, classNode));
                    break;
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, GETFIELD, LLOAD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[1];

                    if (fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Node").getNode().name))) {
                        addField("getHead()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, ALOAD, GETFIELD, ALOAD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[1];

                    if (fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Node").getNode().name))) {
                        addField("getCurrent()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }

        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("Node").getNode().name))) {
                addField("getBuckets()", fieldNode);
                break;
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Deque extends Analyser {
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
            int matchedMethods = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.desc.equalsIgnoreCase(String.format("()L%s;", getClassAnalyser("Node").getNode().name))) {
                    matchedMethods++;
                }
            }

            if (matchedMethods > 5) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("Node").getNode().name))) {
                if (Modifier.isPublic(fieldNode.access)) {
                    addField("getHead()", fieldNode);
                }
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ACONST_NULL, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];
                    addField("getCurrent()", insnToField(fieldInsnNode, classNode));
                    break;
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

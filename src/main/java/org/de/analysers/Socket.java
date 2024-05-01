package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Socket extends Analyser {
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
            if (!Modifier.isPublic(classNode.access) || classNode.superName == null) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equalsIgnoreCase("<init>") && methodNode.desc.contains("(Ljava/net/Socket;")) {
                    return classNode;
                }
            }

            // Fallback to field level check
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equalsIgnoreCase("Ljava/net/Socket;")) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }

            if (fieldNode.desc.equals("Ljava/net/Socket;")) {
                addField("getSocket()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("AsynchronousInputStream").getNode().name))) {
                addField("getInputStream()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("AsynchronousOutputStream").getNode().name))) {
                addField("getOutputStream()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, INVOKEVIRTUAL, ILOAD, INVOKESPECIAL, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.startsWith("L")) {
                        addField("getSize()", insnToField(fieldInsnNode, classNode));
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

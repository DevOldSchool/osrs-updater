package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class AsynchronousOutputStream extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 8;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.interfaces.contains("java/lang/Runnable")) {
                continue;
            }

            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals("Ljava/io/OutputStream;")) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            switch (fieldNode.desc) {
                case "Ljava/io/IOException;":
                    addField("getException()", fieldNode);
                    break;
                case "Ljava/io/OutputStream;":
                    addField("getOutputStream()", fieldNode);
                    break;
                case "Ljava/lang/Thread;":
                    addField("getThread()", fieldNode);
                    break;
                case "[B":
                    addField("getBuffer()", fieldNode);
                    break;
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, GETFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getSize()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_1, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                        addField("isStopped()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getOffset()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, IMUL, ALOAD, GETFIELD, IMUL);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getWriteIndex()", insnToField(fieldInsnNode, classNode));
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

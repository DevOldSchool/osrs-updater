package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class Signlink extends Analyser {
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
        for (ClassNode classNode : classes) {
            if (!classNode.interfaces.contains("java/lang/Runnable")) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, NEW, DUP, ALOAD, INVOKESPECIAL, PUTFIELD, -1, -1, ALOAD, GETFIELD, BIPUSH);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[5];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Ljava/lang/Thread;")) {
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
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, NEW, DUP, ALOAD, INVOKESPECIAL, PUTFIELD, -1, -1, ALOAD, GETFIELD, BIPUSH);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[5];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Ljava/lang/Thread;")) {
                        addField("getThread()", insnToField(fieldInsnNode, classNode));
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

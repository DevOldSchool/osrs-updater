package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class FileSystemRequestHandler extends Analyser {
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
                if (!methodNode.name.equals("run")) {
                    continue;
                }

                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, CHECKCAST);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        TypeInsnNode typeInsnNode = (TypeInsnNode) matches[0];

                        if (typeInsnNode.desc.equals(getClassAnalyser("FileSystemRequest").getNode().name)) {
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
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals("Ljava/lang/Object;")) {
                addField("getObject()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Link extends Analyser {
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
            if (!Modifier.isPublic(classNode.access)) {
                continue;
            }

            int matchedFields = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equalsIgnoreCase(String.format("L%s;", classNode.name))) {
                    matchedFields++;
                }
            }

            if (matchedFields == 2 && classNode.fields.size() == 2) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, ALOAD, GETFIELD, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.desc.equalsIgnoreCase(String.format("L%s;", classNode.name))) {
                        addField("getPrevious()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, ALOAD, GETFIELD, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (!fieldInsnNode.name.equalsIgnoreCase(getField("getPrevious()").getField().name) &&
                            fieldInsnNode.desc.equalsIgnoreCase(String.format("L%s;", classNode.name))) {
                        addField("getNext()", insnToField(fieldInsnNode, classNode));
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

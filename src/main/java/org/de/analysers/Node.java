package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Node extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 3;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals("java/lang/Object")) {
                continue;
            }

            boolean hasLong = false;
            int selfFieldCount = 0;

            for (FieldNode fieldNode : classNode.fields) {
                if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals("J")) {
                    hasLong = true;
                } else if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }
            }

            if (hasLong && selfFieldCount == 2) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        String getNextField = null;

        for (int i = 0; i < classNode.methods.size(); i++) {
            MethodNode methodNode = classNode.methods.get(i);
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ACONST_NULL, PUTFIELD);

            if (instructionSearch.match()) {
                if (getNextField == null) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (fieldInsnNode.desc.equals(String.format("L%s;", classNode.name))) {
                            getNextField = fieldInsnNode.name;
                            addField("getNext()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }
        }

        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals("J") &&
                    (fieldNode.access & Opcodes.ACC_STATIC) == 0) {
                addField("getUid()", fieldNode);
            }

            if (!fieldNode.name.equals(getNextField) &&
                    fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                addField("getPrevious()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

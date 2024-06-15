package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
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
        return 2;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals("java/lang/Object")) {
                continue;
            }

            if (classNode.fields.size() != 3) {
                continue;
            }

            int longCount = 0;
            int selfFieldCount = 0;

            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("J")) {
                    longCount++;
                }

                if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }
            }

            if (longCount == 1 && selfFieldCount == 2) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (!methodNode.name.equals("<init>") && methodNode.desc.equals("()V")) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IFNONNULL);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals(String.format("L%s;", classNode.name))) {
                            addField("getNext()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }
        }

        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }

            if (fieldNode.desc.equals(String.format("L%s;", classNode.name)) &&
                    !fieldNode.name.equals(getField("getNext()").getField().name)) {
                addField("getPrevious()", fieldNode);
            }

            if (fieldNode.desc.equals("J")) {
                addField("uid", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (Modifier.isStatic(methodNode.access)) {
                continue;
            }

            if (!methodNode.name.equals("<init>") && methodNode.desc.equals("()V")) {
                addMethod("remove()", methodNode);
            }

            if (methodNode.desc.equals("()Z")) {
                addMethod("hasNext()", methodNode);
            }
        }
    }
}

package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class InventoryDefinition extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                continue;
            }

            int intCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals("I")) {
                    intCount++;
                }
            }

            int byteBufferMethods = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (Modifier.isStatic(methodNode.access)) {
                    continue;
                }

                if (methodNode.desc.contains(String.format("L%s;", getClassAnalyser("ByteBuffer").getNode().name))) {
                    byteBufferMethods++;
                }
            }

            boolean hasMethod = false;
            boolean hasCache = false;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("()V")) {
                    InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD);
                    if (instructionSearch.match()) {
                        for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                            if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                                hasMethod = true;
                            }
                        }
                    }
                }

                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, BIPUSH, INVOKESPECIAL, PUTSTATIC);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (fieldInsnNode.owner.equals(classNode.name) &&
                                fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Cache").getNode().name))) {
                            hasCache = true;
                        }
                    }
                }
            }

            if (intCount == 1 && byteBufferMethods >= 2 && hasMethod && hasCache) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.desc.equals("I")) {
                        addField("getCapacity()", insnToField(fieldInsnNode, classNode));
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

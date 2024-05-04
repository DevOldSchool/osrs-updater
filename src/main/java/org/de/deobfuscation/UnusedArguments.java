package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.*;

public class UnusedArguments extends Deobfuscator {
    private final Map<String, String> argumentsToRemove = new HashMap<>();

    @Override
    public void onCompletion() {
        label = "unused method arguments";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (Modifier.isInterface(classNode.access)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                removeUnusedArguments(classNode.name, methodNode);
            }
        }

        updateMethodReferences(classes, argumentsToRemove);
        argumentsToRemove.clear();
    }

    private void removeUnusedArguments(String owner, MethodNode methodNode) {
        Set<Integer> usedArgumentIndices = findUsedArgumentIndices(methodNode);

        // Update method descriptor to remove unused arguments
        Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
        StringBuilder newDescBuilder = new StringBuilder("(");

        for (int i = 0; i < argumentTypes.length; i++) {
            if (usedArgumentIndices.contains(i)) {
                newDescBuilder.append(argumentTypes[i].getDescriptor());
            }
        }

        newDescBuilder.append(")").append(Type.getReturnType(methodNode.desc).getDescriptor());
        String newDesc = newDescBuilder.toString();

        if (!newDesc.equals(methodNode.desc)) {
            String originalDesc = methodNode.desc;
            String identifier = owner + "." + methodNode.name + "." + originalDesc;
            argumentsToRemove.put(identifier, newDesc);
            methodNode.desc = newDesc;
            removed++;
        }
    }

    private boolean isLoadInstruction(int opcode) {
        // Check if the opcode represents a load instruction
        return opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD;
    }

    private Set<Integer> findUsedArgumentIndices(MethodNode methodNode) {
        Set<Integer> usedArgumentIndices = new HashSet<>();

        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (instruction instanceof VarInsnNode varInsn) {
                if (isLoadInstruction(varInsn.getOpcode()) && varInsn.var <= Type.getArgumentTypes(methodNode.desc).length) {
                    // Argument usage detected
                    usedArgumentIndices.add(varInsn.var);
                }
            }
        }

        return usedArgumentIndices;
    }

    public void updateMethodReferences(List<ClassNode> classes, Map<String, String> argumentsToRemove) {
        // Collect all methods that reference the method with changed arguments
        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                for (AbstractInsnNode insn : methodNode.instructions) {
                    if (insn instanceof MethodInsnNode methodInsn) {
                        String identifier = methodInsn.owner + "." + methodInsn.name + "." + methodInsn.desc;

                        if (argumentsToRemove.containsKey(identifier)) {
                            methodInsn.desc = argumentsToRemove.get(identifier);
                        }
                    }
                }
            }
        }
    }
}

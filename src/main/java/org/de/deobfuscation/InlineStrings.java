package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.tree.*;

import java.util.*;

public class InlineStrings extends Deobfuscator {
    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Inlined %d strings in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                inlineStringsInMethod(methodNode);
            }
        }
    }

    private void inlineStringsInMethod(MethodNode methodNode) {
        Map<Integer, AbstractInsnNode> variableToStringMap = new HashMap<>();

        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

        // First pass: identify variables assigned string constants
        while (iterator.hasNext()) {
            AbstractInsnNode insnNode = iterator.next();

            if (insnNode instanceof LdcInsnNode ldcInsnNode) {

                // Check if the loaded constant is a string
                if (ldcInsnNode.cst instanceof String) {
                    // Check the next instruction to see if the constant is being stored in a variable
                    try {
                        AbstractInsnNode nextInsn = iterator.next();
                        if (nextInsn instanceof VarInsnNode varInsnNode) {
                            // Store the variable index and the LDC instruction
                            variableToStringMap.put(varInsnNode.var, ldcInsnNode);
                        }
                    } catch (NoSuchElementException ignored) {
                    }
                }
            }
        }

        // Second pass: replace uses of variables with direct loads of constants
        iterator = methodNode.instructions.iterator();

        while (iterator.hasNext()) {
            AbstractInsnNode abstractInsnNode = iterator.next();

            // Look for variable loads
            if (abstractInsnNode instanceof VarInsnNode varInsnNode) {

                // Check if this variable load can be replaced
                if (variableToStringMap.containsKey(varInsnNode.var)) {
                    AbstractInsnNode ldcInsnNode = variableToStringMap.get(varInsnNode.var);

                    // Replace the variable load with the LDC instruction
                    methodNode.instructions.set(abstractInsnNode, ldcInsnNode.clone(null));
                    removed++;
                }
            }
        }
    }
}

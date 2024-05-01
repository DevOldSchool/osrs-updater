package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.*;

public class UnusedMethods extends Deobfuscator {
    @Override
    public void onCompletion() {
        label = "unused methods";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        Set<String> referencedMethods = getReferencedMethods(classes);

        // Remove unused methods
        for (ClassNode classNode : classes) {
            Iterator<MethodNode> methodIterator = classNode.methods.iterator();
            while (methodIterator.hasNext()) {
                MethodNode methodNode = methodIterator.next();

                // If the method is not referenced and not abstract, remove it
                if (!Modifier.isAbstract(methodNode.access) && !referencedMethods.contains(classNode.name + "." + methodNode.name + methodNode.desc)) {
                    methodIterator.remove();
                    removed++;
                }
            }
        }
    }

    public Set<String> getReferencedMethods(List<ClassNode> classes) {
        Set<String> referencedMethods = new HashSet<>();

        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                // Init methods are always referenced
                if (methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) {
                    referencedMethods.add(classNode.name + "." + methodNode.name + methodNode.desc);
                }

                // Ignore JDK methods
                if (isJdkMethod(classNode.name, methodNode.name, methodNode.desc)) {
                    referencedMethods.add(classNode.name + "." + methodNode.name + methodNode.desc);
                }

                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (insnNode instanceof MethodInsnNode methodInsnNode) {
                        referencedMethods.add(methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc);
                    }

                    if (insnNode instanceof FieldInsnNode) {
                        referencedMethods.add(classNode.name + "." + methodNode.name + methodNode.desc);
                    }

                    if (insnNode instanceof TypeInsnNode) {
                        referencedMethods.add(classNode.name + "." + methodNode.name + methodNode.desc);
                    }
                }
            }
        }

        return referencedMethods;
    }

    private boolean isJdkMethod(String owner, String name, String desc) {
        try {
            Class<?>[] classes = {Class.forName(Type.getObjectType(owner).getClassName())};
            while (classes.length > 0) {
                for (Class<?> cls : classes) {
                    for (java.lang.reflect.Method method : cls.getDeclaredMethods()) {
                        if (method.getName().equals(name) && Type.getMethodDescriptor(method).equals(desc)) {
                            return true;
                        }
                    }

                    classes = Arrays.copyOf(cls.getInterfaces(), cls.getInterfaces().length + 1);
                    if (cls.getSuperclass() != null) {
                        classes[cls.getInterfaces().length] = cls.getSuperclass();
                    }
                }
            }
        } catch (Exception ignored) {
            // Do nothing
        }

        return false;
    }
}

package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UnusedMethods extends Deobfuscator {
    @Override
    public void onCompletion() {
        label = "unused methods";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        Set<String> referencedMethods = getReferencedMethods(classes);
        Set<String> inheritedMethods = getInheritedMethods(classes);

        // Remove unused methods
        for (ClassNode classNode : classes) {
            Iterator<MethodNode> methodIterator = classNode.methods.iterator();
            while (methodIterator.hasNext()) {
                MethodNode methodNode = methodIterator.next();
                String methodKey = classNode.name + "." + methodNode.name + methodNode.desc;

                // If the method is not referenced and not an inherited method, remove it
                if (!referencedMethods.contains(methodKey) && !inheritedMethods.contains(methodKey)) {
                    methodIterator.remove();
                    removed++;
                }
            }
        }
    }

    private Set<String> getReferencedMethods(List<ClassNode> classes) {
        Set<String> referencedMethods = new HashSet<>();

        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                // Init methods are always referenced
                if (methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) {
                    referencedMethods.add(classNode.name + "." + methodNode.name + methodNode.desc);
                }

                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    switch (insnNode.getOpcode()) {
                        case Opcodes.INVOKESPECIAL:
                        case Opcodes.INVOKEVIRTUAL:
                        case Opcodes.INVOKEINTERFACE:
                        case Opcodes.INVOKESTATIC:
                        case Opcodes.INVOKEDYNAMIC:
                            if (insnNode instanceof MethodInsnNode methodInsnNode) {
                                ClassNode realOwnerClassNode = matchClassNode(classes, methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);

                                if (realOwnerClassNode != null) {
                                    referencedMethods.add(realOwnerClassNode.name + "." + methodInsnNode.name + methodInsnNode.desc);
                                } else {
                                    referencedMethods.add(methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc);
                                }
                            }
                            break;
                    }
                }
            }
        }

        return referencedMethods;
    }

    public Set<String> getInheritedMethods(List<ClassNode> classes) {
        Set<String> inheritedMethods = new HashSet<>();

        for (ClassNode classNode : classes) {
            // Get methods from interfaces
            for (String interfaceName : classNode.interfaces) {
                ClassNode interfaceNode = findClassNode(classes, interfaceName);
                if (interfaceNode == null) {
                    interfaceNode = findClassNode(classes, interfaceName);
                }
                if (interfaceNode != null) {
                    for (MethodNode methodNode : interfaceNode.methods) {
                        String methodKey = classNode.name + "." + methodNode.name + methodNode.desc;
                        inheritedMethods.add(methodKey);
                    }
                }
            }

            // Get methods from superclasses
            String superClassName = classNode.superName;
            while (superClassName != null) {
                ClassNode superClassNode = findClassNode(classes, superClassName);
                if (superClassNode == null) {
                    superClassNode = findClassNode(classes, superClassName);
                }
                if (superClassNode != null) {
                    for (MethodNode methodNode : superClassNode.methods) {
                        String methodKey = classNode.name + "." + methodNode.name + methodNode.desc;
                        inheritedMethods.add(methodKey);
                    }
                    superClassName = superClassNode.superName;
                } else {
                    superClassName = null;
                }
            }
        }

        return inheritedMethods;
    }

    private ClassNode matchClassNode(List<ClassNode> classes, String className, String methodName, String methodDesc) {
        Set<String> visitedClasses = new HashSet<>();
        return findMethodOwnerRecursively(classes, className, methodName, methodDesc, visitedClasses);
    }

    private ClassNode findMethodOwnerRecursively(List<ClassNode> classes, String className, String methodName, String methodDesc, Set<String> visitedClasses) {
        if (visitedClasses.contains(className)) {
            return null;
        }
        visitedClasses.add(className);

        ClassNode classNode = findClassNode(classes, className);
        if (classNode == null) {
            return null;
        }

        // Check if the classNode contains the method
        List<MethodNode> methods = classNode.methods;
        for (MethodNode method : methods) {
            if (method.name.equals(methodName) && method.desc.equals(methodDesc)) {
                return classNode;
            }
        }

        // Check superclasses and interfaces
        if (classNode.superName != null) {
            ClassNode owner = findMethodOwnerRecursively(classes, classNode.superName, methodName, methodDesc, visitedClasses);
            if (owner != null) {
                return owner;
            }
        }

        for (String interfaceName : classNode.interfaces) {
            ClassNode owner = findMethodOwnerRecursively(classes, interfaceName, methodName, methodDesc, visitedClasses);
            if (owner != null) {
                return owner;
            }
        }

        return null;
    }

    private ClassNode findClassNode(List<ClassNode> classes, String className) {
        for (ClassNode classNode : classes) {
            if (classNode.name.equals(className)) {
                return classNode;
            }
        }

        try {
            InputStream classStream = getClass().getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class");
            if (classStream == null) {
                return null;
            }

            ClassReader classReader = new ClassReader(classStream);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            return classNode;
        } catch (IOException ignored) {
        }

        return null;
    }
}

package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class KeyInputData extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 0;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!Modifier.isAbstract(classNode.access)) {
                continue;
            }

            if (!Modifier.isInterface(classNode.access)) {
                continue;
            }

            boolean hasAllAbstractMethods = true;
            boolean hasZMethod = false;
            for (MethodNode methodNode : classNode.methods) {
                if (!Modifier.isAbstract(methodNode.access)) {
                    hasAllAbstractMethods = false;
                }

                if (methodNode.desc.equals("()Z")) {
                    hasZMethod = true;
                }
            }

            if (hasAllAbstractMethods && hasZMethod) {
//                System.out.println(classNode.name);
                return classNode;
            }
//
//            final boolean[] allAbstractFields = {true};
//            final boolean[] hasZbDescriptor = {false};
//
//            // Load the bytecode of the interface class
//            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//            classNode.accept(classWriter);
//            byte[] classBytes = classWriter.toByteArray();
//
//            ClassReader classReader = new ClassReader(classBytes);
//
//            // Create a ClassVisitor to visit the interface
//            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {
//                @Override
//                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
////                    System.out.println("Method: " + name + ", Descriptor: " + descriptor + " " + Modifier.isAbstract(access));
//
//                    if (!Modifier.isAbstract(access)) {
//                        allAbstractFields[0] = false;
//                    }
//
//                    if (descriptor.equals("(ZB)Z")) {
//                        hasZbDescriptor[0] = true;
//                    }
//
//                    return super.visitMethod(access, name, descriptor, signature, exceptions);
//                }
//            };
//
//            // Visit the interface class to extract method information
//            classReader.accept(classVisitor, ClassReader.SKIP_CODE);
//
//            if (allAbstractFields[0] && hasZbDescriptor[0]) {
////                System.out.println(classNode.name);
//                return classNode;
//            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {

    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldMover extends Deobfuscator {
    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Moved %d fields to original classes in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        FieldOwnerResolver resolver = new FieldOwnerResolver(classes);

        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                for (AbstractInsnNode abstractInsnNode : methodNode.instructions.toArray()) {
                    if (abstractInsnNode instanceof FieldInsnNode fieldInsnNode) {
                        int opCode = fieldInsnNode.getOpcode();
                        String previousOwner = fieldInsnNode.owner;
                        boolean isStatic = opCode == Opcodes.GETSTATIC || opCode == Opcodes.PUTSTATIC;

                        fieldInsnNode.owner = resolver.getOwner(fieldInsnNode.owner, fieldInsnNode.name, fieldInsnNode.desc, isStatic);
                        String newOwner = fieldInsnNode.owner;

                        if (!previousOwner.equals(newOwner)) {
                            removed++;
                        }
                    }
                }
            }
        }
    }

    private static class FieldOwnerResolver {
        private final Map<String, ClassNode> classNames = new HashMap<>();

        public FieldOwnerResolver(List<ClassNode> classes) {
            for (ClassNode classNode : classes) {
                classNames.put(classNode.name, classNode);
            }
        }

        public String getOwner(String owner, String name, String desc, boolean isStatic) {
            ClassNode cls = classNames.get(owner);
            while (cls != null) {
                if (containsField(cls, name, desc, isStatic)) {
                    return cls.name;
                }
                cls = classNames.get(cls.superName);
            }

            return owner;
        }

        private boolean containsField(ClassNode classNode, String name, String desc, boolean isStatic) {
            for (FieldNode field : classNode.fields) {
                if (field.name.equals(name) && field.desc.equals(desc) &&
                        ((field.access & Opcodes.ACC_STATIC) != 0) == isStatic) {
                    return true;
                }
            }

            return false;
        }
    }
}

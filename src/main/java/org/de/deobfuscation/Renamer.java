package org.de.deobfuscation;

import org.de.Analyser;
import org.de.Deobfuscator;
import org.de.Field;
import org.de.Method;
import org.objectweb.asm.tree.*;

import java.util.List;

public class Renamer extends Deobfuscator {
    private int renamedClasses;
    private int renamedMethods;
    private int renamedFields;

    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Renamed %d classes, %d methods and %d fields in %d ms\n", renamedClasses, renamedMethods, renamedFields, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes, List<Analyser> analysers) {
        for (Analyser analyser : analysers) {
            if (analyser.getNode() != null) {
                updateClassReferences(classes, analyser.getObfuscatedName(), analyser.getClass().getSimpleName());
                renamedClasses++;

                for (Method method : analyser.getMethods().values()) {
                    updateMethodReferences(classes, method.getClassNode(), method.getObfuscatedName(), method.getRawName());
                    renamedMethods++;
                }

                for (Field field : analyser.getFields().values()) {
                    updateFieldReferences(classes, field.getClassNode(), field.getObfuscatedName(), field.getRawName());
                    renamedFields++;
                }
            }
        }
    }

    public void updateClassReferences(List<ClassNode> classes, String obfuscatedName, String deobfuscatedName) {
        for (ClassNode classNode : classes) {
            // Update class name references
            if (classNode.name.equals(obfuscatedName)) {
                classNode.name = deobfuscatedName;
            }

            // Update extended class name reference
            if (classNode.superName.equals(obfuscatedName)) {
                classNode.superName = deobfuscatedName;
            }

            // Update field references
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.contains(String.format("L%s;", obfuscatedName))) {
                    fieldNode.desc = fieldNode.desc.replace(obfuscatedName, deobfuscatedName);
                }

                // Update field annotation references
                if (fieldNode.visibleAnnotations != null) {
                    for (AnnotationNode annotationNode : fieldNode.visibleAnnotations) {
                        if (annotationNode.desc.contains(String.format("L%s;", obfuscatedName))) {
                            annotationNode.desc = annotationNode.desc.replace(String.format("L%s;", obfuscatedName), String.format("L%s;", deobfuscatedName));
                        }
                    }
                }

                if (fieldNode.invisibleAnnotations != null) {
                    for (AnnotationNode annotationNode : fieldNode.invisibleAnnotations) {
                        if (annotationNode.desc.contains(String.format("L%s;", obfuscatedName))) {
                            annotationNode.desc = annotationNode.desc.replace(String.format("L%s;", obfuscatedName), String.format("L%s;", deobfuscatedName));
                        }
                    }
                }
            }

            // Update interface references
            for (String interfaceName : classNode.interfaces) {
                if (interfaceName.equals(obfuscatedName)) {
                    classNode.interfaces.remove(interfaceName);
                    classNode.interfaces.add(deobfuscatedName);
                }
            }

            // Update inner class references
            for (InnerClassNode innerClassNode : classNode.innerClasses) {
                if (innerClassNode.name.equals(obfuscatedName)) {
                    innerClassNode.name = deobfuscatedName;
                }
            }

            // Update method references
            for (MethodNode methodNode : classNode.methods) {
                // Update parameters and return types
                if (methodNode.desc.contains(String.format("L%s;", obfuscatedName))) {
                    methodNode.desc = methodNode.desc.replace(obfuscatedName, deobfuscatedName);
                }

                // Update instructions
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    // Field references
                    if (insnNode instanceof FieldInsnNode fieldInsnNode) {
                        if (fieldInsnNode.owner.equals(obfuscatedName)) {
                            fieldInsnNode.owner = deobfuscatedName;
                        }

                        if (fieldInsnNode.desc.contains(String.format("L%s;", obfuscatedName))) {
                            fieldInsnNode.desc = fieldInsnNode.desc.replace(obfuscatedName, deobfuscatedName);
                        }
                    }

                    // Method references
                    if (insnNode instanceof MethodInsnNode methodInsnNode) {
                        if (methodInsnNode.owner.equals(obfuscatedName)) {
                            methodInsnNode.owner = deobfuscatedName;
                        }

                        if (methodInsnNode.desc.contains(String.format("L%s;", obfuscatedName))) {
                            methodInsnNode.desc = methodInsnNode.desc.replace(obfuscatedName, deobfuscatedName);
                        }
                    }

                    // Type references
                    if (insnNode instanceof TypeInsnNode typeInsnNode) {
                        if (typeInsnNode.desc.equals(obfuscatedName)) {
                            typeInsnNode.desc = deobfuscatedName;
                        }
                    }
                }

                // Update try-catch blocks
                for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
                    if (tryCatchBlock.type != null && tryCatchBlock.type.equals(obfuscatedName)) {
                        tryCatchBlock.type = deobfuscatedName;
                    }
                }

                // Update method annotation references
                if (methodNode.visibleAnnotations != null) {
                    for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
                        if (annotationNode.desc.contains(String.format("L%s;", obfuscatedName))) {
                            annotationNode.desc = annotationNode.desc.replace(String.format("L%s;", obfuscatedName), String.format("L%s;", deobfuscatedName));
                        }
                    }
                }

                if (methodNode.invisibleAnnotations != null) {
                    for (AnnotationNode annotationNode : methodNode.invisibleAnnotations) {
                        if (annotationNode.desc.contains(String.format("L%s;", obfuscatedName))) {
                            annotationNode.desc = annotationNode.desc.replace(String.format("L%s;", obfuscatedName), String.format("L%s;", deobfuscatedName));
                        }
                    }
                }
            }
        }
    }

    public void updateMethodReferences(List<ClassNode> classes, ClassNode owner, String obfuscatedName, String deobfuscatedName) {
        for (ClassNode classNode : classes) {
            // Update method references
            for (MethodNode methodNode : classNode.methods) {
                // Update method names
                if (classNode.equals(owner) && methodNode.name.equals(obfuscatedName)) {
                    methodNode.name = deobfuscatedName;
                }

                // Update instructions
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    // Method references
                    if (insnNode instanceof MethodInsnNode methodInsnNode) {
                        if (methodInsnNode.owner.equals(owner.name) && methodInsnNode.name.equals(obfuscatedName)) {
                            methodInsnNode.name = deobfuscatedName;
                        }
                    }
                }
            }
        }
    }

    public void updateFieldReferences(List<ClassNode> classes, ClassNode owner, String obfuscatedName, String deobfuscatedName) {
        for (ClassNode classNode : classes) {
            // Update field references
            for (FieldNode fieldNode : classNode.fields) {
                if (classNode.equals(owner) && fieldNode.name.equals(obfuscatedName)) {
                    fieldNode.name = deobfuscatedName;
                }
            }

            // Update method references
            for (MethodNode methodNode : classNode.methods) {
                // Update instructions
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    // Field references
                    if (insnNode instanceof FieldInsnNode fieldInsnNode) {
                        if (fieldInsnNode.owner.equals(owner.name) && fieldInsnNode.name.equals(obfuscatedName)) {
                            fieldInsnNode.name = deobfuscatedName;
                        }
                    }
                }
            }
        }
    }
}

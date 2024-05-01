package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.Collectors;

public class MethodMover extends Deobfuscator {
    private final Map<String, List<String>> duplicateMethodsMap = new HashMap<>();
    private final Map<MethodNode, ClassNode> methodOwnersMap = new HashMap<>();
    private final Map<String, String> origMethodOwnersMap = new HashMap<>();

    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Moved %d methods to original classes in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        // Collect duplicate static methods
        for (ClassNode classNode : classes) {
            for (MethodNode method : classNode.methods) {

                String fingerprint = methodFingerprint(method);
                String methodNameDesc = classNode.name + "." + method.name + method.desc;
                List<String> methodsList = duplicateMethodsMap.computeIfAbsent(fingerprint, k -> new ArrayList<>());
                methodsList.add(methodNameDesc);
            }
        }

        // Remove methods that are not duplicates
        duplicateMethodsMap.entrySet().removeIf(entry -> entry.getValue().size() == 1);

        // Map original method to its actual owner
        for (ClassNode classNode : classes) {
            for (MethodNode method : classNode.methods) {
                String identifier = classNode.name + "." + method.name + method.desc;

                duplicateMethodsMap.forEach((fingerprint, methods) -> {
                    if (methods.contains(identifier)) {
                        List<String> classNames = new ArrayList<>();

                        for (String s : methods) {
                            classNames.add(s.split("\\.")[0]);
                        }

                        classNames = classNames.stream().distinct().collect(Collectors.toList());
                        String realClassName = classNames.getFirst();

                        if (!classNode.name.equals(realClassName)) {
                            origMethodOwnersMap.put(identifier, realClassName);
                        }
                    }
                });
            }
        }

        // Move static methods to their original classes
        for (Map.Entry<String, String> entry : origMethodOwnersMap.entrySet()) {
            String src = entry.getKey();
            String orig = entry.getValue();

            MethodNode srcMethod = findMethod(classes, src);
            ClassNode srcCls = findClass(classes, src.split("\\.")[0]);
            if (srcMethod == null || srcCls == null) {
                continue;
            }

            ClassNode origCls = findClass(classes, orig);
            if (origCls == null) {
                continue;
            }

            origCls.methods.add(srcMethod);
            methodOwnersMap.put(srcMethod, srcCls);
        }

        // Update method calls to point to the moved methods
        for (ClassNode classNode : classes) {
            for (MethodNode method : classNode.methods) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn instanceof MethodInsnNode methodInsn) {
                        String identifier = methodInsn.owner + "." + methodInsn.name + methodInsn.desc;

                        if (origMethodOwnersMap.containsKey(identifier)) {
//                            System.out.println("Moving method " + methodInsn.name + " " + methodInsn.desc + " from " + methodInsn.owner + " to " + origMethodOwnersMap.get(identifier));
                            methodInsn.owner = origMethodOwnersMap.get(identifier);
                            removed++;
                        }
                    }
                }
            }
        }

        // Remove moved methods from their previous classes
        for (Map.Entry<MethodNode, ClassNode> entry : methodOwnersMap.entrySet()) {
            MethodNode method = entry.getKey();
            ClassNode classNode = entry.getValue();
            classNode.methods.remove(method);
        }
    }

    private String methodFingerprint(MethodNode method) {
        Type returnType = Type.getReturnType(method.desc);
        String lineNumberRange = lineNumberRange(method.instructions) != null ? Arrays.toString(lineNumberRange(method.instructions)) : "*";
        int hash = hash(method.instructions);
        return returnType.toString() + "." + lineNumberRange.replace("[", "").replace("]", "").replace(", ", "..") + "." + hash;
    }

    private int[] lineNumberRange(InsnList instructions) {
        List<Integer> lineNumbers = new ArrayList<>();
        for (AbstractInsnNode insn : instructions.toArray()) {
            if (insn instanceof LineNumberNode) {
                lineNumbers.add(((LineNumberNode) insn).line);
            }
        }

        if (lineNumbers.isEmpty()) {
            return null;
        }

        int min = Collections.min(lineNumbers);
        int max = Collections.max(lineNumbers);

        return new int[]{min, max};
    }

    private int hash(InsnList instructions) {
        Set<String> hashSet = new HashSet<>();
        for (AbstractInsnNode insn : instructions.toArray()) {
            if (insn instanceof FieldInsnNode fieldInsn) {
                hashSet.add(fieldInsn.owner + "." + fieldInsn.name + ":" + fieldInsn.getOpcode());
            } else if (insn instanceof MethodInsnNode methodInsn) {
                hashSet.add(methodInsn.getOpcode() + ":" + methodInsn.owner + "." + methodInsn.name);
            } else if (insn instanceof InsnNode) {
                hashSet.add(String.valueOf(insn.getOpcode()));
            }
        }

        return hashSet.hashCode();
    }

    private ClassNode findClass(List<ClassNode> classNodes, String className) {
        for (ClassNode classNode : classNodes) {
            if (classNode.name.equals(className)) {
                return classNode;
            }
        }

        return null;
    }

    private MethodNode findMethod(List<ClassNode> classNodes, String methodName) {
        for (ClassNode classNode : classNodes) {
            for (MethodNode method : classNode.methods) {
                String identifier = classNode.name + "." + method.name + method.desc;
                if (identifier.equals(methodName)) {
                    return method;
                }
            }
        }

        return null;
    }
}

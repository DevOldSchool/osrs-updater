package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class NanoTimer extends Analyser {
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
            int longCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("J")) {
                    longCount++;
                }
            }

            if (longCount < 1) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (!methodNode.name.equals("<init>")) {
                    continue;
                }

                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, INVOKESTATIC);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) matches[1];

                        if (methodInsnNode.name.equals("nanoTime")) {
                            return classNode;
                        }
                    }
                }
            }
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

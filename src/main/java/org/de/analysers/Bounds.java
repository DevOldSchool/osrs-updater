package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class Bounds extends Analyser {
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
            int intCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                intCount++;
            }

            if (intCount != 4) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("(IIII)V")) {
                    InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, ILOAD, LDC, INVOKEVIRTUAL);
                    if (instructionSearch.match()) {
                        return classNode;
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

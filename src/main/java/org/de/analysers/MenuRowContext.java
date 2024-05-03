package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class MenuRowContext extends Analyser {
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
            int stringCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("I")) {
                    intCount++;
                }

                if (fieldNode.desc.equals("Ljava/lang/String;")) {
                    stringCount++;
                }
            }

            if (intCount != 5 || stringCount != 2) {
                continue;
            }

            int methodCount = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (Modifier.isStatic(methodNode.access)) {
                    continue;
                }

                methodCount++;
            }

            if (methodCount > 1) {
                continue;
            }

            return classNode;
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

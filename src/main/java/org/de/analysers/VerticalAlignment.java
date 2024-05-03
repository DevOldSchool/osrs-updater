package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class VerticalAlignment extends Analyser {
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
            int selfFieldCount = 0;
            int intCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }

                if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals("I")) {
                    intCount++;
                }
            }

            if (selfFieldCount != 3 || intCount != 2) {
                continue;
            }

            int initMethodCount = 0;
            int methodCount = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("(II)V")) {
                    initMethodCount++;
                }

                if (Modifier.isStatic(methodNode.access)) {
                    methodCount++;
                }
            }

            if (initMethodCount != 1 || methodCount != 1) {
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

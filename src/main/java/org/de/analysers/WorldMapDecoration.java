package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class WorldMapDecoration extends Analyser {
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

                if (fieldNode.desc.equals("I")) {
                    intCount++;
                }
            }

            if (intCount != 3) {
                continue;
            }

            int initMethodCount = 0;
            int boolMethodCount = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("(III)V")) {
                    initMethodCount++;
                }

                if (!Modifier.isStatic(methodNode.access) && methodNode.desc.endsWith(")Z")) {
                    boolMethodCount++;
                }
            }

            if (initMethodCount != 1 || boolMethodCount > 0) {
                continue;
            }

//            System.out.println(classNode.name + " " + classNode.superName + " " + initMethodCount + " " + boolMethodCount);
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

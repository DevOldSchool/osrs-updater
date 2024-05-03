package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class AbstractTimer extends Analyser {
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

            if (!classNode.interfaces.isEmpty()) {
                continue;
            }

            if (!classNode.superName.equals("java/lang/Object")) {
                continue;
            }

            int fieldCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                fieldCount++;
            }

            if (fieldCount != 0) {
                continue;
            }

            int constructorMethodCount = 0;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("()V")) {
                    constructorMethodCount++;
                }
            }

            if (constructorMethodCount != 3) {
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

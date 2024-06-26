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
            if (!Modifier.isInterface(classNode.access)) {
                continue;
            }

            boolean hasAllAbstractMethods = true;
            boolean hasBoolMethod = false;
            for (MethodNode methodNode : classNode.methods) {
                if (!Modifier.isAbstract(methodNode.access)) {
                    hasAllAbstractMethods = false;
                }

                if (methodNode.desc.endsWith(")Z")) {
                    hasBoolMethod = true;
                }
            }

            if (hasAllAbstractMethods && hasBoolMethod) {
//                System.out.println(classNode.name);
                return classNode;
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

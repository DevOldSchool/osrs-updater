package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class Sprite extends Analyser {
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
            if (!Modifier.isFinal(classNode.access) ||
                    Modifier.isStatic(classNode.access) ||
                    Modifier.isAbstract(classNode.access)) {
                continue;
            }

            if (classNode.superName.equals(getClassAnalyser("JGraphics").getNode().name)) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.desc.equals("(IIIIIIII[I[I)V")) {
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

package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class ModelHeader extends Analyser {
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
            if (Modifier.isStatic(classNode.access)) {
                continue;
            }

            if (classNode.name.equals(getClassAnalyser("Model").getNode().name)) {
                continue;
            }

            if (classNode.superName.equals(getClassAnalyser("RenderableNode").getNode().name)) {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.desc.equals(String.format("(IIIII)L%s;", getClassAnalyser("Model").getNode().name))) {
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

package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class JFont extends Analyser {
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

            if (classNode.superName.equals(getClassAnalyser("JGraphics").getNode().name)) {
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

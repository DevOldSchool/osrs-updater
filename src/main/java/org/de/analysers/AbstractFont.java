package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class AbstractFont extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 1;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (classNode.name.equals(getClassAnalyser("JFont").getNode().name) ||
                    classNode.name.equals(getClassAnalyser("Sprite").getNode().name)) {
                continue;
            }

            if (classNode.superName.equals(getClassAnalyser("Rasterizer2D").getNode().name)) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals("[[B")) {
                addField("getPixels()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

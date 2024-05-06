package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class ItemLayer extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 2;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals("java/lang/Object")) {
                continue;
            }

            int renderableNodeCount = 0;

            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("RenderableNode").getNode().name))) {
                    renderableNodeCount++;
                }
            }

            if (renderableNodeCount == 3) {
//                System.out.println("FOUND CLASS " + classNode.name + " " + renderableNodeCount);
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("RenderableNode").getNode().name))) {
                addField("getRenderable()", fieldNode);
            }

            if (fieldNode.desc.equals("J")) {
                addField("getHash()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

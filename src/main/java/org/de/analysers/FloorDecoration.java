package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class FloorDecoration extends Analyser {
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
        ClassNode wallDecoration = getClassAnalyser("WallDecoration").getNode();
        ClassNode region = getClassAnalyser("Region").getNode();

        if (wallDecoration == null || region == null) {
            return null;
        }

        for (ClassNode classNode : classes) {
            if (classNode.name.equals(wallDecoration.name)) {
                continue;
            }

            if (!Modifier.isFinal(classNode.access)) {
                continue;
            }

            int intCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals("I")) {
                    intCount++;
                }
            }

            if (intCount != 4) {
                continue;
            }

//            System.out.println("FOUND CLASS " + classNode.name + " " + intCount);

            int renderableNodeFieldCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equalsIgnoreCase(String.format("L%s;", getClassAnalyser("RenderableNode").getNode().name))) {
                    renderableNodeFieldCount++;
//                    System.out.println("MATCHED FIELD " + classNode.name);
                }
            }

            if (renderableNodeFieldCount == 1) {
//                System.out.println("FOUND " + classNode.name + " " + intCount);
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
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

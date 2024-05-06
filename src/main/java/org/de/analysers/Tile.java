package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class Tile extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 9;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("Node").getNode().name)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (!methodNode.name.equals("<init>")) {
                    continue;
                }

                if (methodNode.desc.equals("(III)V")) {
                    return classNode;
                }
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

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("BoundaryObject").getNode().name))) {
                addField("getBoundaryObject()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("FloorDecoration").getNode().name))) {
                addField("getFloorDecoration()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("WallDecoration").getNode().name))) {
                addField("getWallDecoration()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("ItemLayer").getNode().name))) {
                addField("getItemLayer()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("TileModel").getNode().name))) {
                addField("getTileModel()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("TilePaint").getNode().name))) {
                addField("getTilePaint()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("InteractableObject").getNode().name))) {
                addField("getInteractableObjects()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                addField("getTile()", fieldNode);
            }

            if (fieldNode.desc.equals("[I")) {
                addField("getEntityFlags()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class WorldView extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 3;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            int dequeArrayCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals(String.format("[[[L%s;", getClassAnalyser("Deque").getNode().name))) {
                    dequeArrayCount++;
                }
            }

            if (dequeArrayCount != 1) {
                continue;
            }

            return classNode;
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }

            if (fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("Player").getNode().name))) {
                addField("getPlayers()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("Npc").getNode().name))) {
                addField("getNpcs()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("[[[L%s;", getClassAnalyser("Deque").getNode().name))) {
                addField("getGroundItems()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

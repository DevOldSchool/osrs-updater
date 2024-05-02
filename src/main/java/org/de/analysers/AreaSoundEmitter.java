package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class AreaSoundEmitter extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("Node").getNode().name)) {
                continue;
            }

            int audioNodeCount = 0;
            int objectNodeCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("AudioRequestNode").getNode().name))) {
                    audioNodeCount++;
                }

                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("ObjectDefinition").getNode().name))) {
                    objectNodeCount++;
                }
            }

            if (audioNodeCount > 0 && objectNodeCount > 0) {
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

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("ObjectDefinition").getNode().name))) {
                addField("getEmittingObject()", fieldNode);
            }

            if (fieldNode.desc.equals("[I")) {
                addField("getSoundIds()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class CombatBar extends Analyser {
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

            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("NodeList").getNode().name))) {
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

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("NodeList").getNode().name))) {
                addField("getNodeList()", fieldNode);
            }

            if (fieldNode.desc.startsWith("L") && !fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("NodeList").getNode().name))) {
                addField("getDefinition()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

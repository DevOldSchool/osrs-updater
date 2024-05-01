package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class Region extends Analyser {
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
            int tripleI = 0;
            if (classNode.superName.contains("Object")) {
                for (FieldNode fn : classNode.fields) {
                    if (!Modifier.isStatic(fn.access) && !Modifier.isPublic(fn.access)) {
                        if (fn.desc.equals("[[I") || fn.desc.contains("[[[I"))
                            tripleI++;
                    }
                }
                if (tripleI == 4) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("ItemLayer").getNode().name))) {
                addField("getGameObjects()", fieldNode);
            }
            if (Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("ItemLayer").getNode().name))) {
                addField("getGameObjectCache()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class RuneScriptVM extends Analyser {
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
            int mapCount = 0;
            int boolArrayCount = 0;
            int boolCount = 0;
            int longCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("Ljava/util/Map;")) {
                    mapCount++;
                }

                if (fieldNode.desc.equals("[Z")) {
                    boolArrayCount++;
                }

                if (fieldNode.desc.equals("Z")) {
                    boolCount++;
                }

                if (fieldNode.desc.equals("J")) {
                    longCount++;
                }
            }

            if (mapCount != 1 || boolArrayCount != 1 || boolCount != 1 || longCount != 1) {
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

            if (fieldNode.desc.equals("Ljava/util/Map;")) {
                addField("getVarcMap()", fieldNode);
            }

            if (fieldNode.desc.equals("Z")) {
                addField("getChanged()", fieldNode);
            }

            if (fieldNode.desc.equals("J")) {
                addField("getCycle()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

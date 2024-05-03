package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class Hitsplat extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                continue;
            }

            if (classNode.name.equals(getClassAnalyser("ChatboxMessage").getNode().name) ||
                    classNode.name.equals(getClassAnalyser("RuneScript").getNode().name)) {
                continue;
            }

            int stringCount = 0;
            int intArrayCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("Ljava/lang/String;")) {
                    stringCount++;
                }

                if (fieldNode.desc.equals("[I")) {
                    intArrayCount++;
                }
            }

            if (stringCount != 1 || intArrayCount != 1) {
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

            if (fieldNode.desc.equals("Ljava/lang/String;")) {
                addField("getAmount()", fieldNode);
            }

            if (fieldNode.desc.equals("[I")) {
                addField("getTransformIds()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

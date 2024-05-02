package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class ScriptState extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 4;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("RuneScript").getNode().name))) {
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

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("RuneScript").getNode().name))) {
                addField("getInvokedFromScript()", fieldNode);
            }

            if (fieldNode.desc.equals("[Ljava/lang/String;")) {
                addField("getLocalStrings()", fieldNode);
            }

            if (fieldNode.desc.equals("[I")) {
                addField("getLocalInts()", fieldNode);
            }

            if (fieldNode.desc.equals("I")) {
                addField("getStackIndex()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

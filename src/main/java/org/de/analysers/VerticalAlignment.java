package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class VerticalAlignment extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 0;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            int selfFieldCount = 0;
            int intCount = 0;
            int widgetCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isFinal(fieldNode.access) && fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }

                if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals("I")) {
                    intCount++;
                }

                if (Modifier.isStatic(fieldNode.access) &&
                        fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("Widget").getNode().name))) {
                    widgetCount++;
                }
            }

            if (selfFieldCount != 3 || intCount != 2) {
                continue;
            }

            if (classNode.name.equals(getClassAnalyser("HorizontalAlignment").getNode().name)) {
                continue;
            }

//            System.out.println(classNode.name + " " + selfFieldCount + " " + intCount + " " + widgetCount);
            return classNode;
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {

    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

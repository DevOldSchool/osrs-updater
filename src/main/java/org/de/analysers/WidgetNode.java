package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class WidgetNode extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 1;
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
            int i = 0;
            int b = 0;
            for (FieldNode fn : classNode.fields) {
                if (fn.desc.equals("I") && !Modifier.isStatic(fn.access)) {
                    i++;
                }
                if (fn.desc.equals("Z") && !Modifier.isStatic(fn.access)) {
                    b++;
                }
                if (i == 2 && b == 1) {
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

            if (fieldNode.desc.equals("Z")) {
                addField("isRoot()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

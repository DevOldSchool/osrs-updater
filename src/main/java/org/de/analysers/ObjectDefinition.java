package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class ObjectDefinition extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("DualNode").getNode().name)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (!Modifier.isStatic(methodNode.access) &&
                        wildcard(String.format("(II[[IIIIL%s;I*)L%s;", getClassAnalyser("AnimationSequence").getNode().name, getClassAnalyser("Model").getNode().name), methodNode.desc)) {
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

            if (fieldNode.desc.equals("Ljava/lang/String;")) {
                addField("getName()", fieldNode);
            }

            if (fieldNode.desc.equals("[Ljava/lang/String;")) {
                addField("getActions()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

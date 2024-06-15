package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class RenderableNode extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("DualNode").getNode().name)) {
                continue;
            }

            if (Modifier.isAbstract(classNode.access)) {
                for (MethodNode methodNode : classNode.methods) {
                    if (wildcard("(IIIIIIII*)V", methodNode.desc)) {
                        return classNode;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (!Modifier.isStatic(fieldNode.access)) {
                if (fieldNode.desc.equals("I")) {
                    addField("getModelHeight()", fieldNode);
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

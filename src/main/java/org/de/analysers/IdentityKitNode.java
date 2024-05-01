package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class IdentityKitNode extends Analyser {
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
        ClassNode identityKitClassNode = getClassAnalyser("IdentityKit").getNode();

        if (identityKitClassNode == null) {
            return null;
        }

        for (ClassNode classNode : classes) {
            if (classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                for (FieldNode fieldNode : classNode.fields) {
                    if (fieldNode.desc.equals(String.format("[L%s;", identityKitClassNode.name))) {
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
            if (fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("IdentityKit").getNode().name))) {
                addField("getKits()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

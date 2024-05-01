package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class ProjectileComposite extends Analyser {
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
        for (final ClassNode node : classes) {
            if (node.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                if (getField("getProjectileComposite()", getClassAnalyser("Projectile")).getField().desc.equals("L" + node.name + ";")) {
                    return node;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fn : classNode.fields) {
            if (fn.desc.equals("Z")) {
                addField("isMoving()", fn);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

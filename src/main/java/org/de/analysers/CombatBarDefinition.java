package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class CombatBarDefinition extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("DualNode").getNode().name)) {
                continue;
            }

            if (classNode.name.equals(getClassAnalyser("CombatBar").getField("getDefinition()").getField().desc.replaceFirst("L", "").replace(";", ""))) {
                return classNode;
            }
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

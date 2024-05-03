package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class Overlay extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                continue;
            }

            if (classNode.name.equals(getClassAnalyser("InventoryDefinition").getNode().name) ||
                    classNode.name.equals(getClassAnalyser("CombatBarDefinition").getNode().name) ||
                    classNode.name.equals(getClassAnalyser("NpcDefinition").getNode().name) ||
                    classNode.name.equals(getClassAnalyser("Hitsplat").getNode().name)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.desc.equals(String.format("(L%s;IB)V", getClassAnalyser("ByteBuffer").getNode().name))) {
                    return classNode;
                }
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

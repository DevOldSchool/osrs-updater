package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class ChatPlayerComparator extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("NameableComparator").getNode().name)) {
                continue;
            }

            int initMethod = 0;
            int chatPlayerMethodCount = 0;
            String chatPlayerName = getClassAnalyser("ChatPlayer").getNode().name;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("(Z)V")) {
                    initMethod++;
                }

                if (methodNode.desc.equals(String.format("(L%s;L%s;)I", chatPlayerName, chatPlayerName))) {
                    chatPlayerMethodCount++;
                }
            }

            if (initMethod != 1 || chatPlayerMethodCount < 4) {
                continue;
            }

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

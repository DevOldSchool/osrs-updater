package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class ClanMember extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("ChatPlayer").getNode().name)) {
                continue;
            }

            if (classNode.name.equals(getClassAnalyser("FriendMessage").getNode().name)) {
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

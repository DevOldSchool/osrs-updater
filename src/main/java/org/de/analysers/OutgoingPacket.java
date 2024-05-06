package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class OutgoingPacket extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 3;
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

            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("PacketBuffer").getNode().name))) {
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

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("PacketBuffer").getNode().name))) {
                addField("getBuffer()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("OutgoingPacketMeta").getNode().name))) {
                addField("getMeta()", fieldNode);
            }
        }

        InstructionSearcher instructionSearcher;
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<clinit>")) {
                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ICONST_0, PUTSTATIC, RETURN);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getSize()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

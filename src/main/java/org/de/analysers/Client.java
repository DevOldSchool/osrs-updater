package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class Client extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 6;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (classNode.name.equals("client")) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("ApproximateRouteStrategy").getNode().name))) {
                addField("getApproximateRouteStrategy()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("KeyInputHandler").getNode().name))) {
                addField("getKeyInputHandler()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("AudioEffect").getNode().name))) {
                addField("getAudioEffect()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("PlayerDefinition").getNode().name))) {
                addField("getPlayerDefinition()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("GrandExchangeOffer").getNode().name))) {
                addField("getGrandExchangeOffer()", fieldNode);
            }

            if (fieldNode.desc.equals("Lcom/jagex/oldscape/pub/OtlTokenRequester;")) {
                addField("getOtlTokenRequester()", fieldNode);
            }

            if (fieldNode.desc.equals("Lcom/jagex/oldscape/pub/RefreshAccessTokenRequester;")) {
                addField("getRefreshAccessTokenRequester()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<clinit>")) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ICONST_1, PUTSTATIC, -1, -1, PUTSTATIC);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[4];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getWorldId()", insnToField(fieldInsnNode, classNode));
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

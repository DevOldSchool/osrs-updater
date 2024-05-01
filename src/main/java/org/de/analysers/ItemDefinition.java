package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.EIS;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class ItemDefinition extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 9;
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

            for (MethodNode methodNode : classNode.methods) {
                if (wildcard(String.format("(L%s;*)V", classNode.name), methodNode.desc)) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            EIS insn = new EIS(methodNode, "putfield");
            if (insn.found() > 0) {
                FieldInsnNode fin = (FieldInsnNode) insn.getNodesAt(0)[0];
                if (fin.desc.equals("[Ljava/lang/String;")) {
                    addField("getActions()", insnToField(fin, classNode));
                }
            }

            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, LDC, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fin = (FieldInsnNode) matches[2];

                    if (fin.desc.equals("I") && fin.owner.equals(classNode.name)) {
                        addField("getStackIds()", insnToField(fin));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD, ALOAD, ICONST_0, PUTFIELD, ALOAD, ICONST_0, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[8];

                    if (fieldInsnNode.desc.equals("Z") && fieldInsnNode.owner.equals(classNode.name)) {
                        addField("isTradeable()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }
        }

        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }
            if (fieldNode.desc.equals("Ljava/lang/String;")) {
                addField("getName()", fieldNode);
            }
            if (fieldNode.desc.equals("Z")) {
                addField("isMember()", fieldNode);
            }
            if (fieldNode.desc.equals("[Ljava/lang/String;")) {
                addField("getActions()", fieldNode);
            }
            if (fieldNode.desc.equals("[Ljava/lang/String;") && (!fieldNode.name.equals(getField("getActions()").getField().name))) {
                addField("getGroundActions()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

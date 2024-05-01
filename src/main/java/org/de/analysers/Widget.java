package org.de.analysers;

import org.de.Analyser;
import org.de.Updater;
import org.de.utilities.EIS;
import org.de.utilities.InstructionSearcher;
import org.de.utilities.NullInsnNode;
import org.de.utilities.Pattern;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class Widget extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 26;
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
            int count = 0;
            for (FieldNode field : classNode.fields) {
                if (field.desc.equals("[Ljava/lang/Object;"))
                    count++;
            }
            if (count > 20) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (ClassNode cn : Updater.classes) {
            for (MethodNode methodNode : cn.methods) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, GETFIELD, SIPUSH);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[2];
                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Ljava/lang/String;")) {
                            addField("getName()", insnToField(fieldInsnNode));
                            break;
                        }
                    }
                }
            }
            for (MethodNode methodNode : cn.methods) {
                if (!wildcard("(*)V", methodNode.desc)) {
                    continue;
                }
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, IINC, ALOAD, GETFIELD, IFEQ);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[2];
                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                            addField("isHidden()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }

            for (MethodNode mn : cn.methods) {
                if (!wildcard(String.format("([L%s;IIIIIIIII)V", classNode.name), mn.desc) && !Modifier.isFinal(mn.access)) {
                    continue;
                }
                InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, PUTFIELD, ALOAD, ILOAD, -1, -1,
                        PUTFIELD, ILOAD, -1, INVOKESTATIC);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode stackSizes = (FieldInsnNode) insnNodes[5];
                        if (stackSizes.owner.equals(classNode.name) && stackSizes.desc.equals("I")) {
                            addField("getItemStackSize()", insnToField(stackSizes, classNode));
                        }
                    }
                }
                instructionSearcher = new InstructionSearcher(mn.instructions, 0, PUTFIELD, ALOAD, ILOAD, -1, -1, PUTFIELD, ILOAD, -1,
                        INVOKESTATIC);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode itemId = (FieldInsnNode) insnNodes[0];
                        if (itemId.owner.equals(classNode.name) && itemId.desc.equals("I")) {
                            addField("getItemId()", insnToField(itemId, classNode));
                        }
                    }
                }
            }

            for (MethodNode mn : cn.methods) {
                if (!wildcard(String.format("(L%s;*)V", classNode.name), mn.desc)) {
                    continue;
                }
                InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, ALOAD, GETFIELD, -1, -1, BASTORE);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[1];
                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getStaticPosition()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }
            for (MethodNode mn : cn.methods) {
                if (!wildcard("(*)V", mn.desc)) {
                    continue;
                }
                InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, GETFIELD, -1, ISUB, ILOAD, ALOAD, GETFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode scrollx = (FieldInsnNode) insnNodes[0];
                        FieldInsnNode scrolly = (FieldInsnNode) insnNodes[5];
                        if (scrollx.owner.equals(classNode.name) && scrollx.desc.equals("I")) {
                            addField("getScrollX()", insnToField(scrollx, classNode));
                        }
                        if (scrolly.owner.equals(classNode.name) && scrolly.desc.equals("I")) {
                            addField("getScrollY()", insnToField(scrolly, classNode));
                        }
                    }
                }
            }
            for (MethodNode mn : cn.methods) {
                if (!wildcard("(*)V", mn.desc)) {
                    continue;
                }
                InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, ALOAD, GETFIELD, -1, -1);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[1];
                        if (fieldInsnNode == null) {
                            continue;
                        }
                        
                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getIndex()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }
            for (MethodNode mn : cn.methods) {
                if (!wildcard("(*)I", mn.desc)) {
                    continue;
                }
                InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, GETFIELD, ILOAD, IALOAD, IADD, ISTORE);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[0];
                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I")) {
                            addField("getSlotStackSizes()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, GETFIELD, -1, -1, -1, IAND);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[0];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getId()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, DUP_X1, PUTFIELD, LDC, IMUL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[1];
                    FieldInsnNode getx = (FieldInsnNode) insnNodes[4];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.contains("I") &&
                            getx.owner.equals(classNode.name) && getx.desc.contains("I")) {
                        addField("getRelativeX()", insnToField(fieldInsnNode, classNode));
                        addField("getX()", insnToField(getx, classNode));
                        break;
                    }
                }
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, DUP_X1, PUTFIELD, LDC, IMUL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[1];
                    FieldInsnNode gety = (FieldInsnNode) insnNodes[4];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.contains("I") &&
                            gety.owner.equals(classNode.name) && gety.desc.contains("I") &&
                            !fieldInsnNode.name.equals(getField("getRelativeX()").getField().name)) {
                        addField("getRelativeY()", insnToField(fieldInsnNode, classNode));
                        addField("getY()", insnToField(gety, classNode));
                        break;
                    }
                }
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, ILOAD, ALOAD, GETFIELD, ARRAYLENGTH, IF_ICMPGE);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[2];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[Ljava/lang/String;")) {
                        addField("getActions()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, ALOAD, GETFIELD, -1, -1, -1, IF_ICMPNE);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[1];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getParentId()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, ALOAD, GETFIELD, -1, -1, IFLE);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[1];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getBorderThickness()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }

        for (MethodNode mn : classNode.methods) {
            final AbstractInsnNode[] pat = new AbstractInsnNode[]{
                    new VarInsnNode(ALOAD, 0),
                    new FieldInsnNode(GETFIELD, classNode.name, null, "I"),
                    new NullInsnNode(),
                    new NullInsnNode(),
                    new VarInsnNode(ISTORE, 3)
            };
            final AbstractInsnNode[] pattern = Pattern.findPattern(mn, pat);
            if (pattern != null) {
                final FieldInsnNode fin1 = (FieldInsnNode) pattern[1];
                addField("getTextureId()", insnToField(fin1));
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, ALOAD, GETFIELD, -1, -1, BIPUSH, IF_ICMPNE, ALOAD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[1];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getType()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, LDC, INVOKEVIRTUAL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[2];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Ljava/lang/String;")) {
                        addField("getText()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
        for (MethodNode mn : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, GETFIELD, IMUL, LDC, ALOAD, GETFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode width = (FieldInsnNode) insnNodes[0];
                    FieldInsnNode height = (FieldInsnNode) insnNodes[4];
                    if (width.owner.equals(classNode.name) && width.desc.equals("I") &&
                            height.owner.equals(classNode.name) && height.desc.equals("I")) {
                        addField("getHeight()", insnToField(height));
                        addField("getWidth()", insnToField(width));
                        break;
                    }
                }
            }
        }

        for (MethodNode mn : classNode.methods) {
            EIS insn = new EIS(mn, "getfield");
            if (insn.found() > 0) {
                FieldInsnNode fin = (FieldInsnNode) insn.getNodesAt(0)[0];
                if (fin.desc.equals("[I") && insn.found() == 8) {
                    addField("getSlotContentIds()", insnToField(fin, classNode));
                }
            }
        }

        for (FieldNode fn : classNode.fields) {
            if (!Modifier.isStatic(fn.access)) {
                if (fn.desc.equals(String.format("L%s;", getNode().name))) {
                    addField("getParent()", fn);
                }
                if (fn.desc.equals(String.format("[L%s;", getNode().name))) {
                    addField("getChildren()", fn);
                }
                if (fn.desc.equals("[[I")) {
                    addField("getOpcodes()", fn);
                }
                /*if (fn.desc.equals("I") && !fn.equals(getField("getId()").getField())) {
                    addField("getHash()", fn);
                }*/
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}

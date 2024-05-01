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

public class Client extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 40;
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
        for (final MethodNode methodNode : getNode().methods) {
            EIS insn = new EIS(methodNode, "(getstatic|putstatic)");
            if (insn.found() > 0) {
                FieldInsnNode fin = (FieldInsnNode) insn.getNodesAt(0)[0];

                if (fin.desc.equals("L"
                        + getClassAnalyser("HashTable").getNode().name
                        + ";")
                        && insn.found() > 1) {
                    addField("getWidgetNode()", insnToField(fin, classNode));
                }
                if (fin.desc.equals("[L" + getClassAnalyser("Player").getNode().name + ";")) {
                    addField("getPlayers()", insnToField(fin, classNode));
                }
                if (fin.desc.equals("[L" + getClassAnalyser("Npc").getNode().name + ";")) {
                    addField("getNpcs()", insnToField(fin, classNode));
                }
                if (fin.desc.equals("[[[L" + getClassAnalyser("LinkedList").getNode().name + ";")) {
                    addField("getGroundItems()", insnToField(fin, classNode));
                }
            }
        }
        /*
            Backup checking!
         */
        for (FieldNode fin : classNode.fields) {
            if (fin.desc.equals("L"
                    + getClassAnalyser("HashTable").getNode().name
                    + ";")) {
                addField("getWidgetNode()", fin);
            }
            if (fin.desc.equals("[L" + getClassAnalyser("Player").getNode().name + ";")) {
                addField("getPlayers()", fin);
            }
            if (fin.desc.equals("[L" + getClassAnalyser("Npc").getNode().name + ";")) {
                addField("getNpcs()", fin);
            }
            if (fin.desc.equals("[[[L" + getClassAnalyser("LinkedList").getNode().name + ";")) {
                addField("getGroundItems()", fin);
            }
        }
        for (final FieldNode fn : classNode.fields) {
            if (fn.desc.equals("L" + getClassAnalyser("LinkedList").getNode().name + ";")) {
                addField("getProjectiles()", fn);
            }
            if (fn.desc.contains(String.format("[L%s;", getClassAnalyser("CollisionMap").getNode().name))) {
                addField("getCollisionMaps()", fn);
            }
        }

        for (final MethodNode mn : getNode().methods) {
            if (!mn.desc.contains("([L"
                    + getClassAnalyser("Widget").getNode().name
                    + ";IIIIIIII)V")) {
                continue;
            }
            EIS insn = new EIS(mn, "getstatic ifeq aload");
            if (insn.found() > 0) {
                FieldInsnNode fin = (FieldInsnNode) insn.getNodesAt(0)[0];
                if (fin.desc.equals("Z")) {
                    addField("isItemSelected()", insnToField(fin));
                }
            }
        }

        for (final MethodNode mn : getNode().methods) {
            InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, IMUL, ISTORE, GETSTATIC, IFEQ);
            if (is.match()) {
                for (AbstractInsnNode[] ain : is.getMatches()) {
                    FieldInsnNode fin = (FieldInsnNode) ain[2];
                    if (fin.desc.equals("Z") && Modifier.isStatic(insnToField(fin).access)) {
                        addField("isMenuOpen()", insnToField(fin));
                    }
                }
            }
        }

        for (final MethodNode mn : getNode().methods) {
            findCompassAngle(mn);
        }

        for (ClassNode cn : Updater.classes) {
            for (FieldNode fn : cn.fields) {
                if (fn.desc.equals("L" + getClassAnalyser("Player").getNode().name + ";")) {
                    addField("getLocalPlayer()", fn);
                }
                if (fn.desc.equals(String.format("L%s;", getClassAnalyser("Region").getNode().name))) {
                    addField("getRegion()", fn);
                }
                if (fn.desc.equals(String.format("[[L%s;", getClassAnalyser("Widget").getNode().name))) {
                    addField("getWidgets()", fn);
                }
            }

            for (final MethodNode mn : cn.methods) {
                if (!wildcard("(*)V", mn.desc)) {
                    continue;
                }
                InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, GETSTATIC, ILOAD, IALOAD, GETSTATIC,
                        ILOAD, IALOAD, GETSTATIC,
                        ILOAD, IALOAD, GETSTATIC);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode widgx = (FieldInsnNode) ain[3];
                        if (widgx.desc.equals("[I") && Modifier.isStatic(insnToField(widgx).access)) {
                            addField("getWidgetPositionsY()", insnToField(widgx));
                        }
                        FieldInsnNode widgy = (FieldInsnNode) ain[0];
                        if (widgy.desc.equals("[I") && Modifier.isStatic(insnToField(widgy).access)) {
                            addField("getWidgetPositionsX()", insnToField(widgy));
                        }
                    }
                }
            }

            for (final MethodNode mn : cn.methods) {
                AbstractInsnNode[] pat = new AbstractInsnNode[]{
                        new VarInsnNode(ILOAD, 8),
                        new IntInsnNode(SIPUSH, 256),
                        new InsnNode(IMUL),
                        new FieldInsnNode(GETSTATIC, classNode.name, null, "I")
                };
                final AbstractInsnNode[] pattern = Pattern.findPattern(mn, pat);
                if (pattern != null) {
                    final FieldInsnNode fin1 = (FieldInsnNode) pattern[3];
                    addField("getMinimapOffset()", insnToField(fin1));
                }
                pat = new AbstractInsnNode[]{
                        new NullInsnNode(),
                        new FieldInsnNode(GETSTATIC, classNode.name, null, "I"),
                        new InsnNode(IMUL),
                        new NullInsnNode(),
                        new FieldInsnNode(GETSTATIC, classNode.name, null, "I"),
                        new InsnNode(IMUL),
                        new InsnNode(IADD),
                        new IntInsnNode(SIPUSH, 2047)
                };
                final AbstractInsnNode[] pattern2 = Pattern.findPattern(mn, pat);
                if (pattern != null) {
                    final FieldInsnNode fin2 = (FieldInsnNode) pattern2[1];
                    final FieldInsnNode fin3 = (FieldInsnNode) pattern2[4];
                    addField("getMinimapAngle()", insnToField(fin2));
                    addField("getMinimapScale()", insnToField(fin3));
                }
            }

            for (final MethodNode mn : cn.methods) {
                if (!wildcard("(*III)V", mn.desc)) {
                    continue;
                }
                InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, NEW, DUP, INVOKESPECIAL, GETSTATIC);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[3];
                        if (fin.desc.equals("Ljava/lang/String;") && Modifier.isStatic(insnToField(fin).access)) {
                            addField("getSelectedItemName()", insnToField(fin));
                        }
                    }
                }
            }

            for (final MethodNode mn : classNode.methods) {
                final AbstractInsnNode[] pat = new AbstractInsnNode[]{
                        new LdcInsnNode("js5connect"),
                        new IntInsnNode(BIPUSH, 9000),
                        new MethodInsnNode(INVOKEVIRTUAL, null, null, "(Ljava/lang/String;B)V"),
                        new NullInsnNode(),
                        new FieldInsnNode(PUTSTATIC, null, null, "I")
                };
                final AbstractInsnNode[] pattern = Pattern.findPattern(mn, pat);
                if (pattern != null) {
                    final FieldInsnNode fin1 = (FieldInsnNode) pattern[4];
                    addField("getLoginIndex()", insnToField(fin1));
                }
            }

            for (final MethodNode mn : cn.methods) {
                InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, GETSTATIC, ILOAD, ICONST_M1, IASTORE);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[0];
                        if (fin.desc.equals("[I") && Modifier.isStatic(insnToField(fin).access)) {
                            addField("getSettings()", insnToField(fin));
                        }
                    }
                }
            }

            for (final MethodNode mn : cn.methods) {
                InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, ILOAD, IALOAD, ISTORE, GETSTATIC, ILOAD, GETSTATIC, IINC);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[3];
                        if (fin.desc.equals("[I") && Modifier.isStatic(insnToField(fin).access)) {
                            addField("getPlayerSettings()", insnToField(fin));
                        }
                    }
                }
            }

            for (final MethodNode mn : cn.methods) {
                if (!mn.desc.contains("L"
                        + getClassAnalyser("Widget").getNode().name + ";")) {
                    continue;
                }
                EIS insn = new EIS(mn, "getstatic");
                if (insn.found() > 0) {
                    FieldInsnNode fin = (FieldInsnNode) insn.getNodesAt(0)[0];
                    if (fin.desc.equals("[I")) {
                        addField("getSkillLevelArray()", insnToField(fin));
                    }
                }
            }


            for (final MethodNode mn : cn.methods) {
                if (!mn.desc.contains("L"
                        + getClassAnalyser("Widget").getNode().name + ";")) {
                    continue;
                }
                EIS insn = new EIS(mn, "getstatic * * * iaload * * isub");
                if (insn.found() > 0) {
                    FieldInsnNode fin = (FieldInsnNode) insn.getNodesAt(0)[0];
                    if (fin.desc.equals("[I") && !fin.name.equals(getField("getSkillLevelArray()").getField().name)) {
                        addField("getRealSkillLevelArray()", insnToField(fin));
                    }
                }
            }

            for (final MethodNode mn : cn.methods) {
                if (wildcard("(*)I", mn.desc)) {
                    InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, GETSTATIC, ALOAD, ILOAD, IINC);
                    if (is.match()) {
                        for (AbstractInsnNode[] ain : is.getMatches()) {
                            FieldInsnNode fin = (FieldInsnNode) ain[0];
                            if (fin.desc.equals("[I") && Modifier.isStatic(insnToField(fin).access) &&
                                    !isDuplicate(insnToField(fin))) {
                                addField("getSkillExpArray()", insnToField(fin));
                            }
                        }
                    }
                }
            }

            for (MethodNode mn : cn.methods) {
                InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, GETSTATIC, LDC, IMUL, IFLE, ILOAD);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode x = (FieldInsnNode) ain[0];
                        if (Modifier.isStatic(insnToField(x).access) && x.desc.equals("I")) {
                            addField("getMenuOptionCount()", insnToField(x));
                        }
                    }
                }
            }

            for (final MethodNode mn : cn.methods) {
                if (!wildcard("(I)V", mn.desc) || !Modifier.isStatic(mn.access) || !Modifier.isFinal(mn.access)) {
                    continue;
                }

                InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, GETSTATIC, -1, -1, -1,
                        GETSTATIC, -1, -1,
                        GETSTATIC, -1, -1,
                        GETSTATIC);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        final FieldInsnNode fin1 = (FieldInsnNode) ain[0];
                        final FieldInsnNode fin2 = (FieldInsnNode) ain[4];
                        final FieldInsnNode fin3 = (FieldInsnNode) ain[7];
                        final FieldInsnNode fin4 = (FieldInsnNode) ain[10];
                        if (fin1.desc.equals("I") && fin2.desc.equals("I")
                                && fin3.desc.equals("I") && fin4.desc.equals("I")) {
                            addField("getMenuX()", insnToField(fin1));
                            addField("getMenuY()", insnToField(fin2));
                            addField("getMenuWidth()", insnToField(fin3));
                            addField("getMenuHeight()", insnToField(fin4));
                        }
                    }
                }
            }

            for (final MethodNode menuMethod : cn.methods) {
                if (menuMethod.desc.equals("(Ljava/lang/String;Ljava/lang/String;IIIII)V")
                        && Modifier.isStatic(menuMethod.access)) {
                    InstructionSearcher instructionSearcher = new InstructionSearcher(menuMethod.instructions, 0, GETSTATIC,
                            GETSTATIC, -1, -1, -1, AASTORE, GETSTATIC);
                    if (instructionSearcher.match()) {
                        for (AbstractInsnNode[] ain : instructionSearcher.getMatches()) {
                            final FieldInsnNode fin1 = (FieldInsnNode) ain[0];
                            final FieldInsnNode fin2 = (FieldInsnNode) ain[6];
                            if (fin1.desc.equals("[Ljava/lang/String;")) {
                                addField("getMenuActions()", insnToField(fin1));
                            }
                            if (fin2.desc.equals("[Ljava/lang/String;")) {
                                addField("getMenuTargets()", insnToField(fin2));
                            }
                        }
                    }
                }
            }

            for (MethodNode mn : cn.methods) {
                if (!wildcard("(*I)I", mn.desc)) {
                    continue;
                }
                InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, GETSTATIC, -1,
                        -1, -1, GETSTATIC);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] ain : instructionSearcher.getMatches()) {
                        FieldInsnNode fin1 = (FieldInsnNode) ain[0];
                        addField("getBaseX()", insnToField(fin1));
                    }
                }
                instructionSearcher = new InstructionSearcher(mn.instructions, 0, GETSTATIC, -1, GETSTATIC, GETFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] ain : instructionSearcher.getMatches()) {
                        FieldInsnNode fin1 = (FieldInsnNode) ain[0];
                        addField("getBaseY()", insnToField(fin1));
                    }
                }
            }

            /*for (MethodNode mn : cn.methods) {
                if (!wildcard("()V", mn.desc) && !Modifier.isFinal(mn.access)) {
                    continue;
                }
                InstructionSearcher is = new InstructionSearcher(mn.instructions, 0, ISUB, ISTORE, ILOAD, LDC, GETSTATIC, IMUL);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[4];
                        if (Modifier.isStatic(insnToField(fin).access) && fin.desc.equals("I")) {
                            addField("getCameraX()", insnToField(fin), findNode(fin.owner));
                        }
                    }
                }

                is = new InstructionSearcher(mn.instructions, 0, ILOAD, GETSTATIC, LDC, IMUL, ISUB, ISTORE);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[1];
                        if (Modifier.isStatic(insnToField(fin).access) && fin.desc.equals("I") && !fin.owner.equals(node.name)
                                && !fin.name.equals(getField("getCameraX()").getField().name)) {
                            addField("getCameraZ()", insnToField(fin), findNode(fin.owner));
                        }
                    }
                }

                is = new InstructionSearcher(mn.instructions, 0, ISUB, ISTORE, ILOAD, GETSTATIC, -1);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[3];
                        if (Modifier.isStatic(insnToField(fin).access) && fin.desc.equals("I") && !fin.owner.equals(node.name)
                                && !fin.name.equals(getField("getCameraX()").getField().name) &&
                                !fin.name.equals(getField("getCameraZ()").getField().name)) {
                            addField("getCameraY()", insnToField(fin), findNode(fin.owner));
                        }
                    }
                }

                is = new InstructionSearcher(mn.instructions, 0, IMUL, PUTSTATIC, ILOAD, I2D, ILOAD);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[1];
                        if (Modifier.isStatic(insnToField(fin).access) && fin.desc.equals("I")) {
                            addField("getCameraPitch()", insnToField(fin), findNode(fin.owner));
                        }
                    }
                }

                is = new InstructionSearcher(mn.instructions, 0, D2I, SIPUSH, IAND, LDC, IMUL, PUTSTATIC);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[5];
                        if (Modifier.isStatic(insnToField(fin).access) && fin.desc.equals("I")) {
                            if (!insnToField(fin).name.equals(getField("getCameraPitch()").getField().name))
                                addField("getCameraYaw()", insnToField(fin), findNode(fin.owner));
                        }
                    }
                }

                if (wildcard("(*)V", mn.desc)) {
                    is = new InstructionSearcher(mn.instructions, 0, GETSTATIC, -1, GETSTATIC, -1, AALOAD, ILOAD, AALOAD);
                    if (is.match()) {
                        for (AbstractInsnNode[] ain : is.getMatches()) {
                            FieldInsnNode fin = (FieldInsnNode) ain[2];
                            if (Modifier.isStatic(insnToField(fin).access) && fin.desc.equals("I")) {
                                addField("getPlane()", insnToField(fin), findNode(fin.owner));
                            }
                        }
                    }
                }

                is = new InstructionSearcher(mn.instructions, 0, IADD, GETSTATIC, ILOAD, AALOAD, ILOAD);
                if (is.match()) {
                    for (AbstractInsnNode[] ain : is.getMatches()) {
                        FieldInsnNode fin = (FieldInsnNode) ain[1];
                        if (Modifier.isStatic(insnToField(fin).access) && fin.desc.equals("[[[I")) {
                            addField("getTileHeights()", insnToField(fin), findNode(fin.owner));
                        }
                    }
                }

                if (wildcard("(*)I", mn.desc)) {
                    is = new InstructionSearcher(mn.instructions, 0, GETSTATIC, ICONST_1, AALOAD, ILOAD);
                    if (is.match()) {
                        for (AbstractInsnNode[] ain : is.getMatches()) {
                            FieldInsnNode fin = (FieldInsnNode) ain[0];
                            if (Modifier.isStatic(insnToField(fin).access) && fin.desc.equals("[[[B")) {
                                addField("getTileBytes()", insnToField(fin), findNode(fin.owner));
                            }
                        }
                    }
                }
            }*/

            for (final MethodNode mn : cn.methods) {
                AbstractInsnNode[] pat = new AbstractInsnNode[]{
                        new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D"),
                        new InsnNode(D2I),
                        new VarInsnNode(ISTORE, 9000)
                };
                AbstractInsnNode[] find;
                if ((find = Pattern.findPattern(mn, pat)) != null) {
                    pat = new AbstractInsnNode[]{
                            new VarInsnNode(ILOAD, 9000),
                            new InsnNode(I2D),
                            new VarInsnNode(ILOAD, ((VarInsnNode) find[2]).var),
                            new InsnNode(I2D),
                            new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D"),
                            new LdcInsnNode(325.949),
                            new InsnNode(DMUL),
                            new InsnNode(D2I),
                            new IntInsnNode(SIPUSH, 2047),
                            new InsnNode(IAND),
                            new VarInsnNode(ISTORE, 9000)
                    };
                    if ((find = Pattern.findPattern(mn, pat)) != null) {
                        for (int i = 0; i < 3; i++) {
                            pat = new AbstractInsnNode[]{
                                    new InsnNode(IMUL),
                                    new InsnNode(ISUB),
                                    new VarInsnNode(ISTORE, i + 4)
                            };
                            if ((find = Pattern.findPattern(mn, pat)) != null) {
                                AbstractInsnNode test = find[0];
                                AbstractInsnNode ain1 = test.getPrevious();
                                AbstractInsnNode ain2 = test.getPrevious().getPrevious();
                                FieldInsnNode cams = (FieldInsnNode) (ain1.getOpcode() == GETSTATIC ? ain1 : ain2);
                                addField(String.format("getCamera%s()", "XYZ".charAt(i)), insnToField(cams));
                            }
                        }
                    }
                }
            }

            for (MethodNode mn : cn.methods) {
                if (mn.desc.contains(String.format("L%s;", getClassAnalyser("ItemDefinition").getNode().name))) {
                    addMethod("getItemComposite()", mn);
                }

                if (wildcard(String.format("(*)L%s;", getClassAnalyser("ObjectDefinition").getNode().name), mn.desc)) {
                    addMethod("getObjectComposite()", mn);
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }

    private boolean findCompassAngle(final MethodNode mn) {
        AbstractInsnNode[] pat = new AbstractInsnNode[]{
                new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "random", "()D"),
                new LdcInsnNode(120.0D),
                new InsnNode(DMUL),
                new InsnNode(D2I)
        };
        AbstractInsnNode[] ret = Pattern.findPattern(mn, pat);
        if (ret != null) {
            AbstractInsnNode ain = ret[3];
            FieldInsnNode fin = null;
            while (fin == null) {
                if (ain.getOpcode() == PUTSTATIC)
                    fin = (FieldInsnNode) ain;
                ain = ain.getNext();
            }
            addField("getCompassAngle()", insnToField(fin));
            return true;
        }
        return false;
    }
}

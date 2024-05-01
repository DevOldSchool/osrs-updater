package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;

public class MethodSorter extends Deobfuscator {
    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Reordered %d methods in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode cls : classes) {
            List<MethodNode> methods = cls.methods;
            removed += methods.size();
            methods.sort(new MethodComparator());
        }
    }

    private static class MethodComparator implements Comparator<MethodNode> {
        @Override
        public int compare(MethodNode m1, MethodNode m2) {
            // Compare by initializer
            boolean init1 = m1.name.equals("<clinit>");
            boolean init2 = m2.name.equals("<clinit>");
            if (init1 && !init2) {
                return -1;
            }
            if (!init1 && init2) {
                return 1;
            }

            // Compare by constructor
            boolean constr1 = m1.name.equals("<init>");
            boolean constr2 = m2.name.equals("<init>");
            if (constr1 && !constr2) {
                return -1;
            }
            if (!constr1 && constr2) {
                return 1;
            }

            // Compare by static
            boolean static1 = Modifier.isStatic(m1.access);
            boolean static2 = Modifier.isStatic(m2.access);
            if (static1 && !static2) {
                return -1;
            }
            if (!static1 && static2) {
                return 1;
            }

            // Compare by line number
            Integer line1 = getLineNumber(m1);
            Integer line2 = getLineNumber(m2);
            if (line1 != null && line2 != null) {
                int lineCompare = Integer.compare(line1, line2);
                if (lineCompare != 0) {
                    return lineCompare;
                }
            } else if (line1 != null) {
                return -1;
            } else if (line2 != null) {
                return 1;
            }

            // Compare by modifiers
            int modifierCompare = Integer.compare(m2.access & Modifier.methodModifiers(), m1.access & Modifier.methodModifiers());
            if (modifierCompare != 0) {
                return modifierCompare;
            }

            // Compare by return type
            String type1 = Type.getMethodType(m1.desc).getReturnType().getClassName();
            String type2 = Type.getMethodType(m2.desc).getReturnType().getClassName();
            int typeCompare = type1.compareTo(type2);
            if (typeCompare != 0) {
                return typeCompare;
            }

            // Compare by name
            return m1.name.compareTo(m2.name);
        }

        private Integer getLineNumber(MethodNode method) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn instanceof LineNumberNode) {
                    return ((LineNumberNode) insn).line;
                }
            }
            return null;
        }
    }
}

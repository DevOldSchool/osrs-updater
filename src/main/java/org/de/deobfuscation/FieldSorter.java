package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;

public class FieldSorter extends Deobfuscator {
    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Reordered %d fields in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode cls : classes) {
            List<FieldNode> fields = cls.fields;
            removed += fields.size();
            fields.sort(new FieldComparator());
        }
    }

    private static class FieldComparator implements Comparator<FieldNode> {
        @Override
        public int compare(FieldNode f1, FieldNode f2) {
            // Compare by static
            boolean static1 = Modifier.isStatic(f1.access);
            boolean static2 = Modifier.isStatic(f2.access);
            if (static1 && !static2) {
                return -1;
            }
            if (!static1 && static2) {
                return 1;
            }

            // Compare by modifiers
            int modifierCompare = Integer.compare(f2.access & Modifier.fieldModifiers(), f1.access & Modifier.fieldModifiers());
            if (modifierCompare != 0) {
                return modifierCompare;
            }

            // Compare by type
            String type1 = Type.getType(f1.desc).getClassName();
            String type2 = Type.getType(f2.desc).getClassName();
            int typeCompare = type1.compareTo(type2);
            if (typeCompare != 0) {
                return typeCompare;
            }

            // Compare by name
            return f1.name.compareTo(f2.name);
        }
    }
}

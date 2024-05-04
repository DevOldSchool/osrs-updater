package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UnusedFields extends Deobfuscator {
    @Override
    public void onCompletion() {
        label = "unused fields";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        Set<String> referencedFields = getReferencedFields(classes);

        // Remove unused fields
        for (ClassNode classNode : classes) {
            Iterator<FieldNode> fieldIterator = classNode.fields.iterator();
            while (fieldIterator.hasNext()) {
                FieldNode fieldNode = fieldIterator.next();

                // If the field is not referenced, remove it
                if (!referencedFields.contains(classNode.name + "." + fieldNode.name + fieldNode.desc)) {
                    fieldIterator.remove();
                    removed++;
                }
            }
        }
    }

    public Set<String> getReferencedFields(List<ClassNode> classes) {
        Set<String> referencedFields = new HashSet<>();

        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (insnNode instanceof FieldInsnNode fieldInsnNode) {
                        referencedFields.add(fieldInsnNode.owner + "." + fieldInsnNode.name + fieldInsnNode.desc);
                    }
                }
            }
        }

        return referencedFields;
    }
}

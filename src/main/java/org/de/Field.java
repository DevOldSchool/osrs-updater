package org.de;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class Field {
    private final String name;
    private final String obfuscatedName;
    private final ClassNode classNode;
    private final FieldNode fieldNode;
    private int multiplier = 1;

    public Field(String name, ClassNode classNode, FieldNode fieldNode, int multiplier) {
        this.name = name;
        this.obfuscatedName = fieldNode.name;
        this.classNode = classNode;
        this.fieldNode = fieldNode;
        this.multiplier = multiplier;
    }

    public String getName() {
        return name;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public FieldNode getFieldNode() {
        return fieldNode;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public String toString() {
        String out = String.format("\t[> '%s' identified as '%s.%s' -] (%s)", name, classNode.name, fieldNode.name, fieldNode.desc);

        if (fieldNode.desc.equals("I")) {
            out += String.format("\t[ * %d ]", multiplier);
        }

        return out;
    }

    public FieldNode getField() {
        return fieldNode;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public String getRawName() {
        char[] c = name.replaceFirst("^get", "").replaceFirst("^is", "").replace("()", "").toCharArray();
        c[0] += 32;
        return new String(c);
    }
}

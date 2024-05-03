package org.de;

import org.de.utilities.Multipliers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Analyser implements Opcodes {
    private long begin = 0;
    private long end = 0;
    private boolean broken = false;
    public Map<String, Field> fields = new HashMap<>();
    public Map<String, Method> methods = new HashMap<>();
    private ClassNode classNode;
    private String obfuscatedName;

    public abstract int getExpectedFieldsSize();

    public abstract int getExpectedMethodsSize();

    public abstract ClassNode matchClassNode(List<ClassNode> classes);

    public abstract void matchFields(ClassNode classNode);

    public abstract void matchMethods(ClassNode classNode);

    public void execute(List<ClassNode> classes) {
        begin = System.currentTimeMillis();
        classNode = matchClassNode(classes);

        if (classNode == null) {
            broken = true;
            return;
        }

        obfuscatedName = classNode.name;
        matchFields(classNode);
        matchMethods(classNode);

        end = (System.currentTimeMillis() - begin);
    }

    public String toString() {
        return String.format("\n[- %s identified as %s extends %s -]", this.getClass().getSimpleName(), classNode.name, classNode.superName);
    }

    public void print() {
        System.out.println(toString().concat(String.format(" (%s/%s) (%s/%s) in %s ms",
                fields.size(),
                getExpectedFieldsSize(),
                methods.size(),
                getExpectedMethodsSize(),
                end)));

        for (Field field : fields.values()) {
            System.out.println(field.toString());
        }

        for (Method method : methods.values()) {
            System.out.println(method.toString());
        }
    }

    public FieldNode insnToField(FieldInsnNode insn) {
        for (ClassNode classNode : Updater.classes) {
            if (classNode.name.equals(insn.owner)) {
                for (FieldNode fieldNode : classNode.fields) {
                    if (fieldNode.name.equals(insn.name)) {
                        return fieldNode;
                    }
                }
            }
        }

        return null;
    }

    public FieldNode insnToField(FieldInsnNode fieldInsnNode, ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.name.equals(fieldInsnNode.name)) {
                return fieldNode;
            }
        }

        return null;
    }

    public void addField(String name, FieldNode fieldNode) {
        if (fieldNode == null || fields.containsKey(name)) {
            return;
        }

        Field field;
        if (fieldNode.desc != null && fieldNode.desc.equals("I")) {
            try {
                field = new Field(name, classNode, fieldNode, Multipliers.get(classNode.name, fieldNode.name));
            } catch (Exception e) {
                field = new Field(name, classNode, fieldNode, 1);
            }
        } else {
            field = new Field(name, classNode, fieldNode, 1);
        }

        fields.put(name, field);
    }

    public void addMethod(String name, MethodNode methodNode) {
        methods.put(name, new Method(name, classNode, methodNode));
    }

    public boolean isBroken() {
        return broken;
    }

    public int getFieldCount() {
        return fields.size();
    }

    public int getMethodCount() {
        return methods.size();
    }

    public Field getField(String name) {
        return getField(name, this);
    }

    public Field getField(String name, Analyser analyser) {
        for (Field field : analyser.fields.values()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }

        return null;
    }

    public Analyser getClassAnalyser(String name) {
        for (Analyser Analyser : Updater.analysers) {
            if (Analyser.getClass().getSimpleName().equals(name)) {
                return Analyser;
            }
        }

        return null;
    }

    public ClassNode getNode() {
        return classNode;
    }

    public boolean isDuplicate(FieldNode fieldNode) {
        for (Field field : fields.values()) {
            if (field.getField() == fieldNode) {
                return true;
            }
        }

        return false;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public Map<String, Method> getMethods() {
        return methods;
    }
}

package org.de;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Method {
    private final String name;
    private final String obfuscatedName;
    private final ClassNode classNode;
    private final MethodNode methodNode;

    public Method(String name, ClassNode classNode, MethodNode methodNode) {
        this.name = name;
        this.obfuscatedName = methodNode.name;
        this.classNode = classNode;
        this.methodNode = methodNode;
    }

    public String getName() {
        return name;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public String toString() {
        return String.format("\t[> Method '%s' identified as '%s.%s' -] (%s)", name, classNode.name, methodNode.name, methodNode.desc);
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public String getRawName() {
        return name.replace("()", "");
    }
}

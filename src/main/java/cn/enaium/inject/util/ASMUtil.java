package cn.enaium.inject.util;

import cn.enaium.inject.callback.Callback;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Enaium
 */
public class ASMUtil {
    public static int getLoadOpcode(String descriptor) {
        switch (descriptor.replace("[", "")) {
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
                return Opcodes.ILOAD;
            case "J":
                return Opcodes.LLOAD;
            case "D":
                return Opcodes.DLOAD;
            case "F":
                return Opcodes.FLOAD;
            default:
                return Opcodes.ALOAD;
        }
    }

    public static int getReturnOpcode(String descriptor) {
        switch (descriptor.replace("[", "")) {
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
                return Opcodes.IRETURN;
            case "J":
                return Opcodes.LRETURN;
            case "D":
                return Opcodes.DRETURN;
            case "F":
                return Opcodes.FRETURN;
            case "V":
                return Opcodes.RETURN;
            default:
                return Opcodes.ARETURN;
        }
    }

    public static String getDescriptor(Class<?> klass) {
        return Type.getDescriptor(klass);
    }

    public static String getOwner(Class<?> klass) {
        return Type.getDescriptor(klass).substring(1, Type.getDescriptor(klass).length() - 1);
    }

    public static String getArgs(String descriptor) {
        return descriptor.substring(descriptor.indexOf("(") + 1, descriptor.lastIndexOf(")"));
    }

    public static String getReturn(String descriptor) {
        return descriptor.substring(descriptor.lastIndexOf(")") + 1);
    }

    public static String cleanCallback(String descriptor) {
        return descriptor.replace(getDescriptor(Callback.class), "");
    }
}

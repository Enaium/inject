package cn.enaium.inject;

import cn.enaium.inject.annotation.At;
import cn.enaium.inject.annotation.Shadow;
import cn.enaium.inject.callback.Callback;
import cn.enaium.inject.util.ASMUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Enaium
 */
public class Inject {


    private static final ArrayList<ClassNode> classNodes = new ArrayList<>();
    private static final HashMap<String, HashMap<String, String>> mapping = new HashMap<>();

    public static void addConfiguration(String name) {
        addConfiguration(Thread.currentThread().getContextClassLoader(), name);
    }

    public static void addConfiguration(Class<?> klass, String name) {
        addConfiguration(klass.getClassLoader(), name);
    }

    public static void addConfiguration(ClassLoader classLoader, String name) {
        try {
            Configuration configuration = new Gson().fromJson(IOUtils.toString(Objects.requireNonNull(classLoader.getResourceAsStream(name))), Configuration.class);
            for (String accessor : configuration.injects) {
                ClassReader classReader = new ClassReader(IOUtils.toByteArray(Objects.requireNonNull(classLoader.getResourceAsStream(accessor.replace(".", "/") + ".class"))));
                ClassNode classNode = new ClassNode();
                classReader.accept(classNode, 0);
                classNodes.add(classNode);
            }
            if (configuration.remapping != null) {
                mapping.putAll(new Gson().fromJson(IOUtils.toString(Objects.requireNonNull(classLoader.getResourceAsStream(configuration.remapping))), new TypeToken<HashMap<String, HashMap<String, String>>>() {
                }.getType()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Configuration {
        String[] injects;
        String remapping;
    }

    public static byte[] transform(byte[] basic) {
        ClassReader classReader = new ClassReader(basic);
        ClassNode targetNode = new ClassNode();
        classReader.accept(targetNode, 0);

        for (ClassNode injectNode : classNodes) {
            final HashMap<String, String> remapping = new HashMap<>();

            if (mapping.containsKey(injectNode.name)) {
                remapping.putAll(mapping.get(injectNode.name));
            }

            final ArrayList<String> shadows = new ArrayList<>();

            String className = null;
            for (AnnotationNode invisibleAnnotation : injectNode.invisibleAnnotations) {
                Type value = getAnnotationValue(invisibleAnnotation, "value");
                if (value != null && !value.getDescriptor().equals(ASMUtil.getDescriptor(cn.enaium.inject.annotation.Inject.class))) {
                    className = value.getClassName();
                    continue;
                }

                String target = getAnnotationValue(invisibleAnnotation, "target");
                if (target != null && !target.equals("")) {
                    className = target;
                }
            }

            if (className != null) {

                className = remapping.getOrDefault(className, className).replace(".", "/");

                if (!className.equals(targetNode.name)) continue;

                injectNode.fields.stream().filter(field -> field.invisibleAnnotations != null).forEach(field -> field.invisibleAnnotations.stream().filter(annotationNode -> annotationNode.desc.equals(ASMUtil.getDescriptor(Shadow.class))).map(annotationNode -> field.name + field.desc).forEach(shadows::add));
                injectNode.methods.stream().filter(method -> method.invisibleAnnotations != null).forEach(method -> method.invisibleAnnotations.stream().filter(annotationNode -> annotationNode.desc.equals(ASMUtil.getDescriptor(Shadow.class))).map(annotationNode -> method.name + method.desc).forEach(shadows::add));

                for (MethodNode injectMethodNode : injectNode.methods) {
                    String methodName = null;
                    At.Type methodType = null;
                    int methodOrdinal = -1;
                    String methodTarget = "";
                    if (injectMethodNode.invisibleAnnotations != null) {
                        for (AnnotationNode invisibleAnnotation : injectMethodNode.invisibleAnnotations) {
                            methodName = getAnnotationValue(invisibleAnnotation, "name");
                            AnnotationNode atAnnotationNode = getAnnotationValue(invisibleAnnotation, "at");
                            if (atAnnotationNode != null) {
                                String[] type = getAnnotationValue(atAnnotationNode, "type");
                                if (type != null) {
                                    methodType = At.Type.valueOf(type[1]);
                                }
                                Integer ordinal = getAnnotationValue(atAnnotationNode, "ordinal");
                                if (ordinal != null) {
                                    methodOrdinal = ordinal;
                                }
                                String target = getAnnotationValue(atAnnotationNode, "target");
                                if (target != null) {
                                    methodTarget = remapping.getOrDefault(target, target);
                                }
                            }
                        }

                        if (methodName != null && methodType != null) {
                            methodName = remapping.getOrDefault(methodName, methodName);

                            MethodNode injectToTargetMethodNode = new MethodNode(injectMethodNode.access, injectMethodNode.name, injectMethodNode.desc, null, null);
                            injectToTargetMethodNode.instructions.add(injectMethodNode.instructions);
                            for (AbstractInsnNode instruction : injectToTargetMethodNode.instructions) {
                                if (instruction instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
                                    if (fieldInsnNode.owner.equals(injectNode.name) && shadows.contains(fieldInsnNode.name + fieldInsnNode.desc)) {
                                        fieldInsnNode.owner = targetNode.name;
                                    }
                                }
                                if (instruction instanceof MethodInsnNode) {
                                    MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                                    if (methodInsnNode.owner.equals(injectNode.name) && shadows.contains(methodInsnNode.name + methodInsnNode.desc)) {
                                        methodInsnNode.owner = targetNode.name;
                                    }
                                }
                            }
                            targetNode.methods.add(injectToTargetMethodNode);

                            for (MethodNode targetMethodNode : targetNode.methods) {
                                if (!(targetMethodNode.name.equals(methodName))) {
                                    continue;
                                }

                                if (!ASMUtil.getArgs(targetMethodNode.desc).equals(ASMUtil.cleanCallback(ASMUtil.getArgs(injectMethodNode.desc)))) {
                                    continue;
                                }

                                int returnCount = 0;
                                int invokeCount = 0;
                                for (AbstractInsnNode targetInstruction : targetMethodNode.instructions) {
                                    InsnList insnList = new InsnList();

                                    Type[] argumentTypes = Type.getMethodType(targetMethodNode.desc).getArgumentTypes();

                                    //new Callback
                                    if (injectMethodNode.desc.contains(ASMUtil.getDescriptor(Callback.class))) {
                                        insnList.add(new TypeInsnNode(NEW, ASMUtil.getOwner(Callback.class)));
                                        insnList.add(new InsnNode(DUP));
                                        insnList.add(new MethodInsnNode(INVOKESPECIAL, ASMUtil.getOwner(Callback.class), "<init>", "()V"));
                                        insnList.add(new VarInsnNode(ASTORE, argumentTypes.length + 1));
                                    }

                                    //this
                                    insnList.add(new VarInsnNode(ALOAD, 0));

                                    for (int i = 0; i < argumentTypes.length; i++) {
                                        Type argumentType = argumentTypes[i];
                                        //args
                                        insnList.add(new VarInsnNode(ASMUtil.getLoadOpcode(argumentType.getDescriptor()), i + 1));
                                    }

                                    //callback arg
                                    if (injectMethodNode.desc.contains(ASMUtil.getDescriptor(Callback.class))) {
                                        insnList.add(new VarInsnNode(ALOAD, argumentTypes.length + 1));
                                    }

                                    //Invoke inject
                                    insnList.add(new MethodInsnNode(INVOKESPECIAL, className, injectMethodNode.name, injectMethodNode.desc));

                                    //callback getCancel
                                    if (injectMethodNode.desc.contains(ASMUtil.getDescriptor(Callback.class))) {
                                        insnList.add(new VarInsnNode(ALOAD, argumentTypes.length + 1));
                                        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, ASMUtil.getOwner(Callback.class), "getCancel", "()Z"));
                                        LabelNode getCancel = new LabelNode();
                                        insnList.add(new JumpInsnNode(IFEQ, getCancel));
                                        String returnDesc = ASMUtil.getReturn(targetMethodNode.desc).replace("[", "");
                                        switch (returnDesc) {
                                            case "V":
                                                break;
                                            case "Z":
                                            case "C":
                                            case "B":
                                            case "S":
                                            case "I":
                                            case "J":
                                            case "D":
                                            case "F":
                                                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, ASMUtil.getOwner(Callback.class), "getReturnValue" + returnDesc, "()" + returnDesc));
                                            default:
                                                insnList.add(new VarInsnNode(ALOAD, argumentTypes.length + 1));
                                                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, ASMUtil.getOwner(Callback.class), "getReturnValue", "()" + Type.getDescriptor(Object.class)));
                                                insnList.add(new TypeInsnNode(CHECKCAST, returnDesc));
                                        }
                                        insnList.add(new InsnNode(ASMUtil.getReturnOpcode(returnDesc)));
                                        insnList.add(getCancel);
                                        insnList.add(new FrameNode(F_SAME, 0, null, 0, null));
                                    }

                                    //Inject at
                                    switch (methodType) {
                                        case HEAD: {
                                            if (targetInstruction.equals(targetMethodNode.instructions.getFirst())) {
                                                targetMethodNode.instructions.insert(targetInstruction, insnList);
                                            }
                                            break;
                                        }
                                        case TAIL: {
                                            if (targetInstruction.equals(targetMethodNode.instructions.get(targetMethodNode.instructions.size() - 3))) {
                                                targetMethodNode.instructions.insert(targetInstruction, insnList);
                                            }
                                            break;
                                        }
                                        case RETURN: {
                                            if (targetInstruction instanceof InsnNode) {
                                                int opcode = targetInstruction.getOpcode();
                                                if (opcode >= IRETURN && opcode <= RETURN) {
                                                    returnCount++;
                                                    if (methodOrdinal == -1) {
                                                        targetMethodNode.instructions.insertBefore(targetInstruction, insnList);
                                                    } else {
                                                        if (methodOrdinal == returnCount) {
                                                            targetMethodNode.instructions.insertBefore(targetInstruction, insnList);
                                                        }
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                        case INVOKE: {
                                            if (targetInstruction instanceof MethodInsnNode) {
                                                MethodInsnNode targetMethodInstruction = ((MethodInsnNode) targetInstruction);
                                                if (methodTarget.equals("L" + targetMethodInstruction.owner + ";" + targetMethodInstruction.name + targetMethodInstruction.desc)) {
                                                    invokeCount++;
                                                    if (methodOrdinal == -1) {
                                                        targetMethodNode.instructions.insert(targetInstruction, insnList);
                                                    } else {
                                                        if (methodOrdinal == invokeCount) {
                                                            targetMethodNode.instructions.insert(targetInstruction, insnList);
                                                        }
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                        case OVERWRITE: {
                                            targetMethodNode.instructions = insnList;
                                            targetMethodNode.instructions.add(new InsnNode(ASMUtil.getReturnOpcode(targetMethodNode.desc.substring(targetMethodNode.desc.indexOf(")") + 1))));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        targetNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getAnnotationValue(AnnotationNode annotationNode, String key) {
        boolean getNextValue = false;

        if (annotationNode.values == null) {
            return null;
        }

        for (Object value : annotationNode.values) {
            if (getNextValue) {
                return (T) value;
            }
            if (value.equals(key)) {
                getNextValue = true;
            }
        }

        return null;
    }
}

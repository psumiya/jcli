package jcli.core;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class ReflectionCommand {

    public static Object invoke(Class<?> targetClass, String methodName, String[] args) {
        return invoke(null, targetClass, methodName, args);
    }

    public static Object invoke(Object instance, Class<?> targetClass, String methodName, String[] args) {
        // 1. Find method with matching name and parameter count
        Method method = findMethod(targetClass, methodName, args.length)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No public method found: " + methodName + " with " + args.length + " arguments in "
                                + targetClass.getName()));

        // 2. Convert arguments
        Object[] typedArgs = convertArgs(method.getParameterTypes(), args, method.isVarArgs());

        // 3. Invoke
        try {
            return method.invoke(instance, typedArgs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    public static Optional<Method> findMethod(Class<?> targetClass, String methodName, int argCount) {
        return Arrays.stream(targetClass.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> {
                    if (m.isVarArgs()) {
                        // For varargs, we need at least (paramCount - 1) args
                        // e.g. func(String, int...) takes 1 or more args.
                        return argCount >= (m.getParameterCount() - 1);
                    }
                    return m.getParameterCount() == argCount;
                })
                .filter(m -> Arrays.stream(m.getParameterTypes()).allMatch(ReflectionCommand::isTypeSupported))
                .filter(m -> isReturnTypeSupported(m.getReturnType()))
                .sorted(java.util.Comparator.comparingInt(ReflectionCommand::countObjectParams))
                .findFirst();
    }

    private static int countObjectParams(Method m) {
        int count = 0;
        for (Class<?> type : m.getParameterTypes()) {
            if (type == Object.class)
                count++;
        }
        return count;
    }

    private static boolean isReturnTypeSupported(Class<?> type) {
        // Filter out types that don't have a useful toString() representation
        if (java.util.stream.BaseStream.class.isAssignableFrom(type))
            return false;
        if (java.util.Iterator.class.isAssignableFrom(type))
            return false;
        if (java.util.Spliterator.class.isAssignableFrom(type))
            return false;
        if (java.util.Enumeration.class.isAssignableFrom(type))
            return false;
        return true;
    }

    private static boolean isTypeSupported(Class<?> type) {
        return type.isPrimitive() ||
                type == String.class ||
                type == CharSequence.class ||
                type == Integer.class || type == Long.class ||
                type == Boolean.class || type == Double.class ||
                type == Float.class || type == Short.class || type == Byte.class ||
                type.isEnum() ||
                type == Class.class ||
                type == Object.class ||
                type.isArray();
    }

    private static Object[] convertArgs(Class<?>[] types, String[] args, boolean isVarArgs) {
        if (!isVarArgs) {
            Object[] typedArgs = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                typedArgs[i] = convert(types[i], args[i]);
            }
            return typedArgs;
        }

        // Handle Varargs
        int fixedParams = types.length - 1;
        Object[] typedArgs = new Object[types.length];

        // Convert fixed portion
        for (int i = 0; i < fixedParams; i++) {
            typedArgs[i] = convert(types[i], args[i]);
        }

        // Convert varargs portion
        Class<?> varArgType = types[fixedParams].getComponentType();
        int varArgCount = args.length - fixedParams;
        Object varArgsArray = java.lang.reflect.Array.newInstance(varArgType, varArgCount);

        for (int i = 0; i < varArgCount; i++) {
            Object val = convert(varArgType, args[fixedParams + i]);
            java.lang.reflect.Array.set(varArgsArray, i, val);
        }
        typedArgs[fixedParams] = varArgsArray;

        return typedArgs;
    }

    private static Object[] convertArgs(Class<?>[] types, String[] args) {
        return convertArgs(types, args, false);
    }

    public static Object createInstance(Class<?> targetClass, String argument) {
        // Try String constructor
        try {
            return targetClass.getConstructor(String.class).newInstance(argument);
        } catch (Exception e) {
        }

        // Try primitive constructors
        try {
            return targetClass.getConstructor(long.class).newInstance(Long.parseLong(argument));
        } catch (Exception e) {
        }

        try {
            return targetClass.getConstructor(int.class).newInstance(Integer.parseInt(argument));
        } catch (Exception e) {
        }

        throw new IllegalArgumentException(
                "Could not instantiate " + targetClass.getName() + " with argument: " + argument);
    }

    public static Object convert(Class<?> type, String arg) {
        if (type == String.class)
            return arg;
        if (type == int.class || type == Integer.class)
            return Integer.parseInt(arg);
        if (type == long.class || type == Long.class)
            return Long.parseLong(arg);
        if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(arg);
        if (type == double.class || type == Double.class)
            return Double.parseDouble(arg);
        if (type == float.class || type == Float.class)
            return Float.parseFloat(arg);
        if (type == CharSequence.class || type == Object.class)
            return arg;

        if (type.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumType = (Class<? extends Enum>) type;
            return Enum.valueOf(enumType, arg);
        }

        if (type == Class.class) {
            try {
                return Class.forName(arg);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Class not found: " + arg);
            }
        }

        if (type.isArray()) {
            return convertArray(type, arg);
        }

        throw new IllegalArgumentException("Unsupported argument type: " + type.getName());
    }

    private static Object convertArray(Class<?> arrayType, String arg) {
        // Try parsing as JSON first
        try {
            Object parsed = net.minidev.json.JSONValue.parse(arg);
            if (parsed instanceof net.minidev.json.JSONArray) {
                net.minidev.json.JSONArray jsonArray = (net.minidev.json.JSONArray) parsed;
                Class<?> componentType = arrayType.getComponentType();
                Object array = java.lang.reflect.Array.newInstance(componentType, jsonArray.size());

                for (int i = 0; i < jsonArray.size(); i++) {
                    Object element = jsonArray.get(i);
                    // Convert each element (recursively if needed, though simple types mainly)
                    // We treat the element as string for convert(), or ideally convert handles
                    // Object if compatible
                    // But convert takes String arg. We used to do convert(type, String).
                    // Let's coerce element to String for simplicity utilizing existing logic,
                    // or handle raw objects if easy.
                    // For primitives, JSONValue gives Long/Double/Boolean etc.

                    Object val = convert(componentType, String.valueOf(element));
                    java.lang.reflect.Array.set(array, i, val);
                }
                return array;
            }
        } catch (Exception e) {
            // Check if it's just comma separated
        }

        // Fallback: simple comma separation?
        // "1,2,3"
        String[] parts = arg.split(",");
        Class<?> componentType = arrayType.getComponentType();
        Object array = java.lang.reflect.Array.newInstance(componentType, parts.length);
        for (int i = 0; i < parts.length; i++) {
            java.lang.reflect.Array.set(array, i, convert(componentType, parts[i].trim()));
        }
        return array;
    }

    public static String diagnoseError(Class<?> targetClass, String methodName, int argCount) {
        java.util.List<Method> candidates = Arrays.stream(targetClass.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .collect(java.util.stream.Collectors.toList());

        if (candidates.isEmpty()) {
            return "No method found with name '" + methodName + "'";
        }

        // Check for return type issues
        Optional<Method> returnTypeMismatch = candidates.stream()
                .filter(m -> m.getParameterCount() == argCount)
                .filter(m -> Arrays.stream(m.getParameterTypes()).allMatch(ReflectionCommand::isTypeSupported))
                .filter(m -> !isReturnTypeSupported(m.getReturnType()))
                .findFirst();

        if (returnTypeMismatch.isPresent()) {
            return "Method '" + methodName + "' exists but is unsupported because it returns '"
                    + returnTypeMismatch.get().getReturnType().getName() + "'";
        }

        // Check for parameter type issues
        Optional<Method> paramTypeMismatch = candidates.stream()
                .filter(m -> m.getParameterCount() == argCount)
                .filter(m -> !Arrays.stream(m.getParameterTypes()).allMatch(ReflectionCommand::isTypeSupported))
                .findFirst();

        if (paramTypeMismatch.isPresent()) {
            return "Method '" + methodName + "' exists but has unsupported parameter types.";
        }

        // Check for arg count issues
        return "Method '" + methodName + "' exists but requires different arguments (found " + candidates.size()
                + " overload(s)).";
    }

    public static String listMethods(Class<?> targetClass) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(targetClass.getMethods())
                .filter(m -> Arrays.stream(m.getParameterTypes()).allMatch(ReflectionCommand::isTypeSupported))
                .filter(m -> isReturnTypeSupported(m.getReturnType()))
                .sorted(java.util.Comparator.comparing(Method::getName))
                .forEach(m -> {
                    sb.append(m.getName()).append("(");
                    Class<?>[] params = m.getParameterTypes();
                    for (int i = 0; i < params.length; i++) {
                        sb.append(params[i].getSimpleName());
                        if (i < params.length - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append(")\n");
                });
        return sb.toString();
    }

    public static String resolveArgument(String arg) {
        if (arg == null)
            return null;
        if (arg.equals("-")) {
            try {
                java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = System.in.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                return result.toString("UTF-8").trim();
            } catch (Exception e) {
                throw new RuntimeException("Failed to read from stdin", e);
            }
        }
        if (arg.startsWith("@")) {
            try {
                return java.nio.file.Files.readString(java.nio.file.Path.of(arg.substring(1))).trim();
            } catch (Exception e) {
                throw new RuntimeException("Failed to read file: " + arg.substring(1), e);
            }
        }
        return arg;
    }
}

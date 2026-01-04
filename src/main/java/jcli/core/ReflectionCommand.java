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
        Object[] typedArgs = convertArgs(method.getParameterTypes(), args);

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
                .filter(m -> m.getParameterCount() == argCount)
                .filter(m -> Arrays.stream(m.getParameterTypes()).allMatch(ReflectionCommand::isTypeSupported))
                .filter(m -> isReturnTypeSupported(m.getReturnType()))
                .findFirst();
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
                type == Class.class;
    }

    private static Object[] convertArgs(Class<?>[] types, String[] args) {
        Object[] typedArgs = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            typedArgs[i] = convert(types[i], args[i]);
        }
        return typedArgs;
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
        if (type == CharSequence.class)
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

        throw new IllegalArgumentException("Unsupported argument type: " + type.getName());
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
}

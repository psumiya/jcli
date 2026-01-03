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
        Method method = findMethod(targetClass, methodName, args.length);

        // 2. Convert arguments
        Object[] typedArgs = convertArgs(method.getParameterTypes(), args);

        // 3. Invoke
        try {
            return method.invoke(instance, typedArgs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    private static Method findMethod(Class<?> targetClass, String methodName, int argCount) {
        Optional<Method> match = Arrays.stream(targetClass.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> m.getParameterCount() == argCount)
                .findFirst();

        return match.orElseThrow(() -> new IllegalArgumentException(
                "No public method found: " + methodName + " with " + argCount + " arguments in "
                        + targetClass.getName()));
    }

    private static Object[] convertArgs(Class<?>[] types, String[] args) {
        Object[] typedArgs = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            typedArgs[i] = convert(types[i], args[i]);
        }
        return typedArgs;
    }

    private static Object convert(Class<?> type, String arg) {
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
        if (type == CharSequence.class)
            return arg; // Handle CharSequence for methods like contains interface

        // Basic fallback
        return arg;
    }

    public static String listMethods(Class<?> targetClass) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(targetClass.getMethods())
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

package jcli.core;

import jcli.config.CommandConfig;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

@CommandLine.Command
public class UniversalCommand implements Runnable {

    private final CommandConfig config;
    private final Class<?> targetClass;

    @Spec
    CommandSpec spec;

    @CommandLine.Option(names = "--methods", description = "Lists all public methods.")
    boolean listMethods;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "Show this help message and exit.")
    boolean helpRequested;

    @CommandLine.Parameters(index = "0", description = "The method name to execute.", arity = "0..1")
    String method;

    @CommandLine.Parameters(index = "1..*", description = "Arguments for the method.")
    String[] args = new String[0];

    public UniversalCommand(CommandConfig config) {
        this.config = config;
        try {
            this.targetClass = Class.forName(config.className());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + config.className(), e);
        }
    }

    @Override
    public void run() {
        try {
            if (listMethods) {
                System.out.println(ReflectionCommand.listMethods(targetClass));
                return;
            }

            if (method == null) {
                System.err.println("Error: Missing required argument '<method>'");
                CommandLine.usage(this, System.out);
                return;
            }

            // Resolve arguments (file/stdin expansion)
            for (int i = 0; i < args.length; i++) {
                args[i] = ReflectionCommand.resolveArgument(args[i]);
            }
            executeStrategy();

        } catch (Exception e) {
            System.err.println("Error executing method '" + method + "': " + e.getMessage());
            if (System.getProperty("jcli.debug") != null) {
                e.printStackTrace();
            }
        }
    }

    private void executeStrategy() throws Exception {
        switch (config.strategy()) {
            case STATIC:
                executeStatic();
                break;
            case INSTANCE:
                executeInstance();
                break;
            case HYBRID:
                executeHybrid();
                break;
        }
    }

    private void executeStatic() {
        Object result = ReflectionCommand.invoke(null, targetClass, method, args);
        System.out.println(result);
    }

    private void executeInstance() {
        if (args.length < 1) {
            throw new IllegalArgumentException("Instance command requires at least one argument (the instance)");
        }
        String instanceText = args[0];
        String[] realArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        Object instance = instanceText;
        Object result = ReflectionCommand.invoke(instance, targetClass, method, realArgs);
        System.out.println(result);
    }

    private void executeHybrid() throws Exception {
        // 1. Try static match with ALL args
        Optional<Method> staticMatch = ReflectionCommand.findMethod(targetClass, method, args.length);
        if (staticMatch.isPresent() && Modifier.isStatic(staticMatch.get().getModifiers())) {
            Object result = ReflectionCommand.invoke(null, targetClass, method, args);
            System.out.println(result);
            return;
        }

        // 2. Try Instance Match with Default Constructor
        // If class has default constructor, we can try using it as the instance
        // and passing ALL args to the method.
        boolean hasDefaultConstructor = java.util.Arrays.stream(targetClass.getConstructors())
                .anyMatch(c -> c.getParameterCount() == 0);

        if (hasDefaultConstructor) {
            Optional<Method> instanceMatch = ReflectionCommand.findMethod(targetClass, method, args.length);
            if (instanceMatch.isPresent() && !Modifier.isStatic(instanceMatch.get().getModifiers())) {
                Object instance = targetClass.getConstructor().newInstance();
                Object result = ReflectionCommand.invoke(instance, targetClass, method, args);
                System.out.println(result);
                return;
            }
        }

        // 3. Try Instance Match with Argument 0 as Instance Source
        // (Legacy behavior / String constructor behavior)
        if (args.length > 0) {
            Optional<Method> instanceMatch = ReflectionCommand.findMethod(targetClass, method, args.length - 1);
            if (instanceMatch.isPresent() && !Modifier.isStatic(instanceMatch.get().getModifiers())) {
                String instanceText = args[0];
                String[] realArgs = java.util.Arrays.copyOfRange(args, 1, args.length);

                Object instance;
                if (config.factory() != null) {
                    instance = ReflectionCommand.invoke(null, targetClass, config.factory(),
                            new String[] { instanceText });
                } else if (targetClass.equals(String.class)) {
                    instance = instanceText;
                } else {
                    instance = ReflectionCommand.createInstance(targetClass, instanceText);
                }

                Object result = ReflectionCommand.invoke(instance, targetClass, method, realArgs);
                System.out.println(result);
                return;
            }
        }

        // Diagnosis
        String error = ReflectionCommand.diagnoseError(targetClass, method, args.length);
        if (args.length > 0) {
            String instanceError = ReflectionCommand.diagnoseError(targetClass, method, args.length - 1);
            if (!instanceError.startsWith("No method found") && !instanceError.contains("different arguments")) {
                error = instanceError;
            }
        }

        throw new IllegalArgumentException(error);
    }
}

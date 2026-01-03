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
            this.targetClass = Class.forName(config.getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + config.getClassName(), e);
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
                // If method is missing, Picocli should handle help if requested,
                // but if not requested, show error.
                // However, since we use usageHelp=true, Picocli handles help BEFORE run().
                // So if we are here, help was NOT requested, but method is missing.
                System.err.println("Error: Missing required argument '<method>'");
                CommandLine.usage(this, System.out);
                return;
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
        switch (config.getStrategy()) {
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
        // First argument is the instance/text
        if (args.length < 1) {
            throw new IllegalArgumentException("Instance command requires at least one argument (the instance)");
        }
        String instanceText = args[0];
        String[] realArgs = java.util.Arrays.copyOfRange(args, 1, args.length);

        // For String, the instance IS the text. For others, we might need a factory.
        // Currently, we treat the first argument as the instance (e.g. String).
        Object instance = instanceText;

        Object result = ReflectionCommand.invoke(instance, targetClass, method, realArgs);
        System.out.println(result);
    }

    private void executeHybrid() throws Exception {
        // Try exact match (static or implicit instance)
        Optional<Method> exactMatch = ReflectionCommand.findMethod(targetClass, method, args.length);
        if (exactMatch.isPresent()) {
            Method m = exactMatch.get();
            if (Modifier.isStatic(m.getModifiers())) {
                // Static method call
                Object result = ReflectionCommand.invoke(null, targetClass, method, args);
                System.out.println(result);
                return;
            }
            // If strictly instance method with no args, it falls through to instance check
            // logic.
        }

        // Strategy:
        // 1. Check if method exists with N args. If static -> run.
        // 2. If not found or instance method, check if we have N+1 args (1 for
        // instance).

        // Check for static match with current args
        Optional<Method> staticMatch = ReflectionCommand.findMethod(targetClass, method, args.length);
        if (staticMatch.isPresent() && Modifier.isStatic(staticMatch.get().getModifiers())) {
            Object result = ReflectionCommand.invoke(null, targetClass, method, args);
            System.out.println(result);
            return;
        }

        // Check for instance match with (args - 1)
        if (args.length > 0) {
            Optional<Method> instanceMatch = ReflectionCommand.findMethod(targetClass, method, args.length - 1);
            if (instanceMatch.isPresent() && !Modifier.isStatic(instanceMatch.get().getModifiers())) {
                String instanceText = args[0];
                String[] realArgs = java.util.Arrays.copyOfRange(args, 1, args.length);

                Object instance;
                if (config.getFactory() != null) {
                    // Invoke factory method (static) to get instance
                    // e.g. Instant.parse(text)
                    instance = ReflectionCommand.invoke(null, targetClass, config.getFactory(),
                            new String[] { instanceText });
                } else {
                    throw new IllegalStateException(
                            "Factory method required for Hybrid command execution on instances");
                }

                Object result = ReflectionCommand.invoke(instance, targetClass, method, realArgs);
                System.out.println(result);
                return;
            }
        }

        throw new IllegalArgumentException("No matching method found: " + method);
    }
}

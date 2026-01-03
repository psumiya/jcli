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

    private void printHelp() {
        System.out.println("Usage: jcli " + config.getName() + " [OPTIONS] <method> [args...]");
        System.out.println();
        System.out.println("Description:");
        System.out.println("  " + config.getDescription());
        System.out.println();

        if (config.getExamples() != null && !config.getExamples().isEmpty()) {
            System.out.println("Examples:");
            for (String ex : config.getExamples()) {
                System.out.println("  " + ex);
            }
            System.out.println();
        }

        System.out.println("Options:");
        System.out.println("  --methods   List all public methods.");
        System.out.println("  -h, --help  Show this help message.");
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
        Object instance = instanceText;

        // If the class is NOT String, and we have a factory, use it?
        // Wait, for String command, "text" is the instance.
        // For generic instance commands, usually we need to parse the first arg into an
        // object.
        // But for this specific task, let's assume String-like behavior or look at
        // current implementations.
        // StringCommand just uses the text.
        // We will assume "INSTANCE" strategy implies the first arg is the object
        // representation (String).
        // Since we only support String currently for 'text', this works for
        // StringCommand.
        // If we want to support other types in INSTANCE mode, we might need a factory.

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
            } else {
                // Instance method with "current" instance (e.g. Instant.now())
                if (targetClass == java.time.Instant.class && method.equals("now")) {
                    // This is actually a static method 'now()', wait.
                    // Instant.now() IS static.
                    // The hybrid logic in InstantCommand.java handles `now` as static.
                    // But `getEpochSecond` is instance.
                    // If I call `jcli instant getEpochSecond`, I need an instance.
                    // The current InstantCommand implementation assumes if static match found ->
                    // run it.
                    // If not, check if we have instance args.
                }
            }
        }

        // If we are here, either no match found, or strictly instance method on
        // provided arg.

        // Try static match first regardless (covered above technically, but let's be
        // robust)
        // Actually, ReflectionCommand.invoke handles finding the method.
        // We can just try to find a static method matching args.

        // Let's refine Hybrid based on InstantCommand logic:
        // 1. Check if method exists with N args.
        // 2. If yes, and static -> invoke.
        // 3. If yes, and instance -> invoke on "default" instance? No, invoke on
        // provided instance?
        // InstantCommand logic:
        // - Find method with N args. If static -> run. (e.g. now(), parse(text))
        // - If not found or instance method, check if we have N+1 args (1 for
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

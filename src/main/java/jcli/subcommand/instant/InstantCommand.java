package jcli.subcommand.instant;

import picocli.CommandLine;

@CommandLine.Command(name = "instant", description = {
        "Methods from java.time.Instant.",
        "",
        "Usage: jcli instant <method> [args...]",
        "",
        "Examples:",
        "  jcli instant now",
        "  jcli instant parse '2023-01-01T00:00:00Z'",
        "  jcli instant --methods"
}, mixinStandardHelpOptions = true)
public class InstantCommand implements Runnable {

    @CommandLine.Option(names = "--methods", description = "Lists all public methods of java.time.Instant.")
    boolean listMethods;

    @CommandLine.Parameters(index = "0", description = "The method name to execute (e.g., now, parse).", arity = "0..1")
    String method;

    @CommandLine.Parameters(index = "1..*", description = "Arguments for the method.")
    String[] args = new String[0];

    @Override
    public void run() {
        try {
            if (listMethods) {
                System.out.println(jcli.core.ReflectionCommand.listMethods(java.time.Instant.class));
                return;
            }

            if (method == null) {
                System.err.println("Missing required parameter: <method>");
                System.err.println("Usage: jcli instant <method> [args...]");
                System.err.println("   or: jcli instant --methods");
                return;
            }

            // Strategy 1: Exact Match (Static Method or Implicit Instance 'now')
            java.util.Optional<java.lang.reflect.Method> exactMatch = jcli.core.ReflectionCommand
                    .findMethod(java.time.Instant.class, method, args.length);
            if (exactMatch.isPresent()) {
                java.lang.reflect.Method m = exactMatch.get();
                Object instance = null;
                if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                    instance = java.time.Instant.now();
                }
                Object result = jcli.core.ReflectionCommand.invoke(instance, java.time.Instant.class, method, args);
                System.out.println(result);
                return;
            }

            // Strategy 2: Explicit Instance Match (Instance Method with first arg as
            // instance)
            if (args.length > 0) {
                java.util.Optional<java.lang.reflect.Method> explicitMatch = jcli.core.ReflectionCommand
                        .findMethod(java.time.Instant.class, method, args.length - 1);
                if (explicitMatch.isPresent()) {
                    java.lang.reflect.Method m = explicitMatch.get();
                    if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                        String instanceText = args[0];
                        String[] realArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
                        Object instance = java.time.Instant.parse(instanceText);

                        Object result = jcli.core.ReflectionCommand.invoke(instance, java.time.Instant.class, method,
                                realArgs);
                        System.out.println(result);
                        return;
                    }
                }
            }

            System.err.println("No matching method found: " + method + " (tried args=" + args.length + " and args="
                    + (args.length - 1) + ")");
        } catch (Exception e) {
            System.err.println("Error executing method '" + method + "': " + e.getMessage());
            if (System.getProperty("jcli.debug") != null) {
                e.printStackTrace();
            }
        }
    }
}

package jcli.subcommand.uuid;

import picocli.CommandLine;

@CommandLine.Command(name = "uuid", description = {
        "Methods from java.util.UUID.",
        "",
        "Usage: jcli uuid <method> [args...]",
        "",
        "Examples:",
        "  jcli uuid randomUUID",
        "  jcli uuid fromString '38400000-8cf0-11bd-b23e-10b96e4ef00d'",
        "  jcli uuid --methods"
}, mixinStandardHelpOptions = true)
public class UuidCommand implements Runnable {

    @CommandLine.Option(names = "--methods", description = "Lists all public methods of java.util.UUID.")
    boolean listMethods;

    @CommandLine.Parameters(index = "0", description = "The method name to execute (e.g., randomUUID, fromString).", arity = "0..1")
    String method;

    @CommandLine.Parameters(index = "1..*", description = "Arguments for the method.")
    String[] args = new String[0];

    @Override
    public void run() {
        try {
            if (listMethods) {
                System.out.println(jcli.core.ReflectionCommand.listMethods(java.util.UUID.class));
                return;
            }

            if (method == null) {
                System.err.println("Missing required parameter: <method>");
                System.err.println("Usage: jcli uuid <method> [args...]");
                System.err.println("   or: jcli uuid --methods");
                return;
            }

            // UUID methods (like randomUUID) are static, so we pass null as instance/text.
            // Arguments start from index 0 relative to the method call.
            Object result = jcli.core.ReflectionCommand.invoke(null, java.util.UUID.class, method, args);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error executing method '" + method + "': " + e.getMessage());
            if (System.getProperty("jcli.debug") != null) {
                e.printStackTrace();
            }
        }
    }
}

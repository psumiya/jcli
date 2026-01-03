package jcli.subcommand.math;

import picocli.CommandLine;

@CommandLine.Command(name = "math", description = {
        "Methods from java.lang.Math.",
        "",
        "Usage: jcli math <method> [args...]",
        "",
        "Examples:",
        "  jcli math abs -10",
        "  jcli math max 10 20",
        "  jcli math sqrt 16",
        ""
}, mixinStandardHelpOptions = true)
public class MathCommand implements Runnable {

    @CommandLine.Option(names = "--methods", description = "Lists all public methods of java.lang.Math.")
    boolean listMethods;

    @CommandLine.Parameters(index = "0", description = "The method name to execute (e.g., abs, max, sqrt).", arity = "0..1")
    String method;

    @CommandLine.Parameters(index = "1..*", description = "Arguments for the method.")
    String[] args = new String[0];

    @Override
    public void run() {
        try {
            if (listMethods) {
                System.out.println(jcli.core.ReflectionCommand.listMethods(Math.class));
                return;
            }

            if (method == null) {
                System.err.println("Missing required parameter: <method>");
                System.err.println("Usage: jcli math <method> [<args>...]");
                System.err.println("   or: jcli math --methods");
                return;
            }

            // Math methods are static, so we pass null as the instance
            Object result = jcli.core.ReflectionCommand.invoke(null, Math.class, method, args);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error executing method '" + method + "': " + e.getMessage());
            if (System.getProperty("jcli.debug") != null) {
                e.printStackTrace();
            }
        }
    }
}

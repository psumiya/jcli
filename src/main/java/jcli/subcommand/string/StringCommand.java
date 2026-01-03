package jcli.subcommand.string;

import picocli.CommandLine;

@CommandLine.Command(name = "string", description = {
        "Methods from java.lang.String.",
        "",
        "Usage: jcli string <method> [text] [args...]",
        "",
        "Examples:",
        "  jcli string length 'hello'",
        "  jcli string substring 'hello world' 0 5",
        "  jcli string startsWith 'hello' 'he'",
        "",
        "Note: Use single quotes '' for text containing special characters like '!' to avoid shell expansion."
}, mixinStandardHelpOptions = true)
public class StringCommand implements Runnable {

    @CommandLine.Option(names = "--methods", description = "Lists all public methods of java.lang.String.")
    boolean listMethods;

    @CommandLine.Parameters(index = "0", description = "The method name to execute (e.g., length, substring, startsWith).", arity = "0..1")
    String method;

    @CommandLine.Parameters(index = "1", description = "The input string to operate on.", arity = "0..1")
    String text;

    @CommandLine.Parameters(index = "2..*", description = "Arguments for the method.")
    String[] args = new String[0];

    @Override
    public void run() {
        try {
            if (listMethods) {
                System.out.println(jcli.core.ReflectionCommand.listMethods(String.class));
                return;
            }

            if (method == null) {
                System.err.println("Missing required parameter: <method>");
                System.err.println("Usage: jcli string <method> <text> [<args>...]");
                System.err.println("   or: jcli string --methods");
                return;
            }

            if (text == null) {
                System.err.println("Missing required parameter: <text>");
                System.err.println("Usage: jcli string <method> <text> [<args>...]");
                return;
            }

            Object result = jcli.core.ReflectionCommand.invoke(text, String.class, method, args);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error executing method '" + method + "': " + e.getMessage());
            if (System.getProperty("jcli.debug") != null) {
                e.printStackTrace();
            }
        }
    }
}

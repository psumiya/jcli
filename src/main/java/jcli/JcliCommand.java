package jcli;

import jcli.core.NoInputTemplate;
import jcli.core.type.NoParam;
import jcli.subcommand.string.StringCommand;
import jcli.subcommand.time.TimeCommand;
import jcli.subcommand.util.UtilCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "jcli", description = "Sugared wrappers to execute methods of JDK and JVM-based libraries from the command line.", mixinStandardHelpOptions = true, subcommands = {
        TimeCommand.class,
        UtilCommand.class,
        StringCommand.class
})
public class JcliCommand implements NoInputTemplate {

    @CommandLine.Option(names = { "-v", "--verbose" }, description = "Print welcome message.")
    boolean verbose;

    NoParam function;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JcliCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void init() {
        if (verbose) {
            function = NoParam.hello;
        }
    }

    @Override
    public boolean isValid() {
        if (function == null) {
            throw new RuntimeException("Missing function to execute.");
        }
        return true;
    }

    @Override
    public NoParam getFunction() {
        return this.function;
    }

}

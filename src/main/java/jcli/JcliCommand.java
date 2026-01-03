package jcli;

import jcli.subcommand.string.StringCommand;
import jcli.subcommand.instant.InstantCommand;
import jcli.subcommand.uuid.UuidCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "jcli", description = "Sugared wrappers to execute methods of JDK and JVM-based libraries from the command line.", mixinStandardHelpOptions = true, subcommands = {
        InstantCommand.class,
        UuidCommand.class,
        StringCommand.class
})
public class JcliCommand implements Runnable {

    @CommandLine.Option(names = { "-v", "--verbose" }, description = "Print welcome message.")
    boolean verbose;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JcliCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (verbose) {
            System.out.println("Welcome to Jcli! Type `jcli -h` to view help manual.");
        }
    }
}

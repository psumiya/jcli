package jcli;

import jcli.config.CommandConfig;
import jcli.config.CommandLoader;
import jcli.core.UniversalCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.List;

@Command(name = "jcli", description = "Sugared wrappers to execute methods of JDK and JVM-based libraries from the command line.", mixinStandardHelpOptions = true)
public class JcliCommand implements Runnable {

    @CommandLine.Option(names = { "-v", "--verbose" }, description = "Print welcome message.")
    boolean verbose;

    public static void main(String[] args) {
        int exitCode = createCommandLine().execute(args);
        System.exit(exitCode);
    }

    public static CommandLine createCommandLine() {
        CommandLine cmd = new CommandLine(new JcliCommand());

        // Dynamically register commands
        try {
            List<CommandConfig> configs = CommandLoader.loadCommands();
            for (CommandConfig config : configs) {
                UniversalCommand command = new UniversalCommand(config);
                // We need to set the name describing the command for help message
                CommandLine subCmd = new CommandLine(command);
                subCmd.getCommandSpec().name(config.getName());
                subCmd.getCommandSpec().usageMessage().description(config.getDescription());

                if (config.getExamples() != null && !config.getExamples().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\nExamples:");
                    for (String ex : config.getExamples()) {
                        sb.append("\n  ").append(ex);
                    }
                    subCmd.getCommandSpec().usageMessage().footer(sb.toString());
                }

                cmd.addSubcommand(config.getName(), subCmd);
            }
        } catch (Exception e) {
            System.err.println("Failed to load commands: " + e.getMessage());
            if (System.getProperty("jcli.debug") != null) {
                e.printStackTrace();
            }
        }
        return cmd;
    }

    @Override
    public void run() {
        if (verbose) {
            System.out.println("Welcome to Jcli! Type `jcli -h` to view help manual.");
        }
    }
}

package jcli;

import io.micronaut.configuration.picocli.PicocliRunner;
import jcli.output.Writer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.UUID;

@Command(name = "jcli", description = "...",
        mixinStandardHelpOptions = true)
public class JcliCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "Print welcome message.")
    boolean verbose;

    @Option(names = {"-uuid", "--uuid"}, description = "Generate a UUID")
    boolean uuid;

    public static void main(String[] args) {
        PicocliRunner.run(JcliCommand.class, args);
    }

    public void run() {
        // business logic here
        if (verbose) {
            Writer.print("Welcome to jcli!");
        }
        if (uuid) {
            Writer.print(UUID.randomUUID());
        }
    }
}

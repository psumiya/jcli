package jcli;

import io.micronaut.configuration.picocli.PicocliRunner;
import jcli.time.Time;
import jcli.util.Util;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "jcli", description = "...",
        mixinStandardHelpOptions = true,
        subcommands = {
                Time.class,
                Util.class
        })
public class JcliCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "Print welcome message.")
    boolean verbose;

    public static void main(String[] args) {
        PicocliRunner.run(JcliCommand.class, args);
    }

    public void run() {
        // business logic here
        if (verbose) {
            System.out.println("Welcome to jcli!");
        }
    }
}

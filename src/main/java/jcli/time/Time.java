package jcli.time;

import picocli.CommandLine;

import java.time.Instant;

@CommandLine.Command(name = "time", description = "Methods from java.time",
        mixinStandardHelpOptions = true)
public class Time implements Runnable {

    @CommandLine.Option(names = {"-now", "--now"}, description = "Prints the value of Instant.now()")
    boolean now;

    @CommandLine.Option(names = {"-toEpochMilli", "--toEpochMilli", "-toEpochMillis", "--toEpochMillis"}, description = "Prints the value of Instant.now().toEpochMillis()")
    boolean toEpochMilli;

    public void run() {
        Instant instant = Instant.now();
        if (now && toEpochMilli) {
            System.out.println(instant.toEpochMilli());
            System.out.println(instant);
        } else if (toEpochMilli) {
            System.out.println(instant.toEpochMilli());
        } else if (now) {
            System.out.println(instant);
        }
    }

}

package jcli.time;

import picocli.CommandLine;

import java.time.Instant;

@CommandLine.Command(name = "time", description = "Methods from java.time",
        mixinStandardHelpOptions = true)
public class Time implements Runnable {

    @CommandLine.Option(names = {"-now", "--now"}, description = "Prints the value of current time as standard formatted date/time.")
    boolean now;

    @CommandLine.Option(names = {"-toEpochMilli", "--toEpochMilli", "-toEpochMillis", "--toEpochMillis"}, description = "Prints the value of current time as the number of milliseconds from the epoch of 1970-01-01T00:00:00Z.")
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

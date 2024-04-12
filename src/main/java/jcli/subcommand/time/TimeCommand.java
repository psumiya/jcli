package jcli.subcommand.time;

import jcli.core.type.NoParam;
import jcli.core.NoInputTemplate;
import picocli.CommandLine;

@CommandLine.Command(name = "time", description = "Methods from java.time",
        mixinStandardHelpOptions = true)
public class TimeCommand implements NoInputTemplate {

    @CommandLine.Option(names = {"-now", "--now"}, description = "Prints the value of current time as standard formatted date/time.")
    boolean now;

    @CommandLine.Option(names = {"-toEpochMilli", "--toEpochMilli", "-toEpochMillis", "--toEpochMillis"}, description = "Prints the value of current time as the number of milliseconds from the epoch of 1970-01-01T00:00:00Z.")
    boolean toEpochMilli;

    @CommandLine.Option(names = {"-function", "--function", "-fn", "--fn"}, description = "Possible Values: ${COMPLETION-CANDIDATES}")
    NoParam function;

    @Override
    public void init() {
        if (now) {
            function = NoParam.now;
        } else if (toEpochMilli) {
            function = NoParam.toEpochMilli;
        }
    }

    @Override
    public boolean isValid() {
        if (this.function == null) {
            throw new RuntimeException("Missing function to execute.");
        }
        return true;
    }

    @Override
    public NoParam getFunction() {
        return this.function;
    }

}

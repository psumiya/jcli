package jcli.subcommand.util;

import jcli.core.type.NoParam;
import jcli.core.NoInputTemplate;
import picocli.CommandLine;

@CommandLine.Command(name = "util", description = "Methods from java.util",
        mixinStandardHelpOptions = true)
public class UtilCommand implements NoInputTemplate {

    @CommandLine.Option(names = {"-uuid", "--uuid", "--randomUUID"}, description = "Generate a UUID")
    boolean uuid;

    @CommandLine.Option(names = {"-function", "--function", "-fn", "--fn"}, description = "Possible Values: ${COMPLETION-CANDIDATES}")
    NoParam function;

    @Override
    public void init() {
        if (uuid) {
            function = NoParam.uuid;
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

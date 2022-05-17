package jcli.util;

import picocli.CommandLine;

import java.util.UUID;

@CommandLine.Command(name = "util", description = "Methods from java.util",
        mixinStandardHelpOptions = true)
public class Util implements Runnable {

    @CommandLine.Option(names = {"-uuid", "--uuid", "--randomUUID"}, description = "Generate a UUID")
    boolean uuid;

    @Override
    public void run() {
        if (uuid) {
            System.out.println(UUID.randomUUID());
        }
    }
}

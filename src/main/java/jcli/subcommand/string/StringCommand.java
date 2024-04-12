package jcli.subcommand.string;

import picocli.CommandLine;

@CommandLine.Command(name = "string", description = "Methods from java.lang.String",
        mixinStandardHelpOptions = true)
public class StringCommand implements Runnable {

    @CommandLine.Option(names = {"-length", "--length", "-len", "--len", "-size", "--size"}, description = "Prints the length of the string.")
    boolean length;

    @CommandLine.Option(names = {"-substring", "--substring"}, description = "Prints the substring in the given range.")
    boolean substring;

    @CommandLine.Option(names = {"-text", "--text"}, description = "Primary string to operate on")
    String text;

    @CommandLine.Option(names = {"-beginIndex", "-from"}, description = "beginIndex for substring")
    private Integer beginIndex;

    @CommandLine.Option(names = {"-endIndex", "-to"}, description = "endIndex for substring")
    private Integer endIndex;

    @Override
    public void run() {
        if (length) {
            System.out.println(text.length());
        } else if (substring) {
            if (endIndex != null && beginIndex != null) {
                System.out.println(text.substring(beginIndex, endIndex));
            } else if (beginIndex != null) {
                System.out.println(text.substring(beginIndex));
            } else {
                System.err.println("Missing option beginIndex or endIndex for substring.");
            }
        }
    }
}

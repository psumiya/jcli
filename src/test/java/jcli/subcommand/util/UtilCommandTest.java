package jcli.subcommand.util;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import jcli.JcliCommand;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilCommandTest {

    @Test
    public void testWithCommand() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "util", "-uuid" };
            PicocliRunner.run(JcliCommand.class, ctx, args);

            // check length
            int length = baos.toString().length() - 1;
            assertTrue(length == 36, "Should be length of a UUID");
        }
    }

    @Test
    public void testWithFunction() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "util", "-fn=uuid" };
            PicocliRunner.run(JcliCommand.class, ctx, args);

            // check length
            int length = baos.toString().length() - 1;
            assertTrue(length == 36, "Should be length of a UUID");
        }
    }

}

package jcli.subcommand.time;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import jcli.JcliCommand;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeCommandTest {

    @Test
    public void testNow() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "time", "-now" };
            PicocliRunner.run(JcliCommand.class, ctx, args);

            // check length
            int length = baos.toString().length() - 1;
            assertTrue(length == 27, "Should be length of a timestamp, e.g. 2022-06-13T11:39:37.318582Z");
        }
    }

    @Test
    public void testNowWithFunction() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "time", "-fn=now" };
            PicocliRunner.run(JcliCommand.class, ctx, args);

            // check length
            int length = baos.toString().length() - 1;
            assertTrue(length == 27, "Should be length of a timestamp, e.g. 2022-06-13T11:39:37.318582Z");
        }
    }

    @Test
    public void testToEpochMilli() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "time", "-fn=toEpochMilli" };
            PicocliRunner.run(JcliCommand.class, ctx, args);

            // check length
            int length = baos.toString().length() - 1;
            assertTrue(length == 13, "Should be length of epoch millis, e.g. 1655120563215");
        }
    }

}

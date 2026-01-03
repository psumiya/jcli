package jcli.subcommand.util;

import jcli.JcliCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilCommandTest {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(baos));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testWithCommand() {
        String[] args = new String[] { "util", "-uuid" };
        new CommandLine(new JcliCommand()).execute(args);

        // check length
        int length = baos.toString().trim().length();
        assertTrue(length == 36, "Should be length of a UUID, got: " + baos.toString());
    }

    @Test
    public void testWithFunction() {
        String[] args = new String[] { "util", "-fn=uuid" };
        new CommandLine(new JcliCommand()).execute(args);

        // check length
        int length = baos.toString().trim().length();
        assertTrue(length == 36, "Should be length of a UUID, got: " + baos.toString());
    }

}

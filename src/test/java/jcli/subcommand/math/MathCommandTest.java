package jcli.subcommand.math;

import jcli.JcliCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MathCommandTest {

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
    public void testAbs() {
        String[] args = new String[] { "math", "abs", "-50" };
        new CommandLine(new JcliCommand()).execute(args);

        String output = baos.toString().trim();
        assertTrue(output.equals("50") || output.equals("50.0"), "Expected 50 or 50.0 but got: " + output);
    }

    @Test
    public void testMax() {
        String[] args = new String[] { "math", "max", "10", "20" };
        new CommandLine(new JcliCommand()).execute(args);

        String output = baos.toString().trim();
        assertTrue(output.equals("20") || output.equals("20.0"), "Expected 20 or 20.0 but got: " + output);
    }

    @Test
    public void testSqrt() {
        String[] args = new String[] { "math", "sqrt", "16" };
        new CommandLine(new JcliCommand()).execute(args);

        String output = baos.toString().trim();
        assertEquals("4.0", output);
    }

    @Test
    public void testListMethods() {
        String[] args = new String[] { "math", "--methods" };
        new CommandLine(new JcliCommand()).execute(args);

        String output = baos.toString().trim();
        assertTrue(output.contains("abs(int)"));
        assertTrue(output.contains("max(int, int)"));
        assertTrue(output.contains("sqrt(double)"));
    }
}

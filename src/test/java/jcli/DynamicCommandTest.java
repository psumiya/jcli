package jcli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicCommandTest {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final ByteArrayOutputStream baes = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUp() {
        System.setProperty("jcli.config.resource", "test-commands.yaml");
        System.setOut(new PrintStream(baos));
        System.setErr(new PrintStream(baes));
    }

    @AfterEach
    public void tearDown() {
        System.clearProperty("jcli.config.resource");
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testDynamicStringCommand() {
        String[] args = new String[] { "test-string", "length", "hello" };
        JcliCommand.createCommandLine().execute(args);

        String output = baos.toString().trim();
        assertEquals("5", output);
    }

    @Test
    public void testDynamicMathCommand() {
        String[] args = new String[] { "test-math", "max", "10", "20" };
        JcliCommand.createCommandLine().execute(args);

        String output = baos.toString().trim();
        assertTrue(output.contains("20"), "Expected 20 but got: " + output);
    }
}

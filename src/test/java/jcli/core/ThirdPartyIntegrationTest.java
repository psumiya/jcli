package jcli.core;

import jcli.config.CommandConfig;
import jcli.core.InstanceStrategy;
import jcli.core.ReflectionCommand;
import jcli.core.UniversalCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThirdPartyIntegrationTest {

    @Test
    public void testJsonPathReadSimple() {
        // Test: jcli JsonPath read '{"a":1}' "$.a"
        CommandConfig config = new CommandConfig(
                "JsonPath",
                "com.jayway.jsonpath.JsonPath",
                InstanceStrategy.STATIC, // Or Hybrid, but read is static
                null,
                "Jayway JsonPath",
                Collections.emptyList());

        UniversalCommand cmd = new UniversalCommand(config);
        cmd.method = "read";
        cmd.args = new String[] { "{\"a\":1}", "$.a" };

        String output = captureOutput(cmd);
        assertEquals("1", output.trim());
    }

    @Test
    public void testJsonPathReadWithVarargs() {
        // Test internal logic of finding varargs method
        // JsonPath.read(String json, String path, Predicate... filters)
        // We pass 2 args, so filters should be empty array

        CommandConfig config = new CommandConfig(
                "JsonPath",
                "com.jayway.jsonpath.JsonPath",
                InstanceStrategy.STATIC,
                null,
                "Jayway JsonPath",
                Collections.emptyList());

        UniversalCommand cmd = new UniversalCommand(config);
        cmd.method = "read";
        cmd.args = new String[] { "{\"a\":1}", "$.a" }; // 2 args, varargs empty

        String output = captureOutput(cmd);
        assertEquals("1", output.trim());
    }

    @Test
    public void testJsonPathReadWithFileExpansion(@TempDir Path tempDir) throws Exception {
        // Test: jcli JsonPath read @file "$.key"
        Path file = tempDir.resolve("data.json");
        Files.writeString(file, "{\"key\":\"value\"}");

        CommandConfig config = new CommandConfig(
                "JsonPath",
                "com.jayway.jsonpath.JsonPath",
                InstanceStrategy.STATIC,
                null,
                "Jayway JsonPath",
                Collections.emptyList());

        UniversalCommand cmd = new UniversalCommand(config);
        cmd.method = "read";
        cmd.args = new String[] { "@" + file.toAbsolutePath().toString(), "$.key" };

        String output = captureOutput(cmd);
        assertEquals("value", output.trim());
    }

    @Test
    public void testReflectionCommandResolveArgument(@TempDir Path tempDir) throws Exception {
        // Simple file
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "content");

        String resolved = ReflectionCommand.resolveArgument("@" + file.toAbsolutePath());
        assertEquals("content", resolved);

        // Normal string
        assertEquals("normal", ReflectionCommand.resolveArgument("normal"));
    }

    private String captureOutput(Runnable task) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(bo));
        try {
            task.run();
        } finally {
            System.setOut(oldOut);
        }
        return bo.toString();
    }
}

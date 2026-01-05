package jcli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentationTestRunner {

    @Test
    void testDocumentedCommands() throws IOException {
        File dataFile = new File("docs/data.json");
        if (!dataFile.exists()) {
            System.out.println("docs/data.json not found. Skipping test. Run 'gradle generateDocsJson' first.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(dataFile);
        JsonNode commands = root.path("commands");

        List<String> failures = new ArrayList<>();
        int successCount = 0;

        Iterator<JsonNode> elements = commands.elements();
        while (elements.hasNext()) {
            JsonNode commandNode = elements.next();
            JsonNode methods = commandNode.path("methods");
            Iterator<JsonNode> methodElements = methods.elements();

            while (methodElements.hasNext()) {
                JsonNode methodNode = methodElements.next();
                if (methodNode.has("example")) {
                    String example = methodNode.get("example").asText();
                    System.out.println("Testing example: " + example);

                    try {
                        String[] args = parseArgs(example);
                        // args[0] is "jcli", remove it
                        if (args.length > 0 && "jcli".equals(args[0])) {
                            String[] realArgs = new String[args.length - 1];
                            System.arraycopy(args, 1, realArgs, 0, realArgs.length);

                            // Execute in-process to simplify setup
                            // Note: This relies on JcliCommand.createCommandLine() logic
                            CommandLine cmd = JcliCommand.createCommandLine();
                            int exitCode = cmd.execute(realArgs);

                            if (exitCode != 0) {
                                failures.add("Failed (Exit Code " + exitCode + "): " + example);
                            } else {
                                successCount++;
                            }
                        }
                    } catch (Exception e) {
                        failures.add("Exception: " + example + " -> " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("Verified " + successCount + " examples successfully.");

        if (!failures.isEmpty()) {
            System.err.println("Failures:");
            failures.forEach(System.err::println);
        }

        assertTrue(failures.isEmpty(), "There were " + failures.size() + " failed documentation examples.");
    }

    // Simple argument parser handling quotes
    private String[] parseArgs(String command) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                currentToken.append(c);
            }
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens.toArray(new String[0]);
    }
}

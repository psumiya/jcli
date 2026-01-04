package jcli.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import jcli.core.InstanceStrategy;

public class CommandLoader {

    public static List<CommandConfig> loadCommands() {
        Yaml yaml = new Yaml();
        List<CommandConfig> configs = new ArrayList<>();

        // 1. Load explicit commands from commands.yaml
        String configResource = System.getProperty("jcli.config.resource", "commands.yaml");
        try (InputStream inputStream = CommandLoader.class.getClassLoader().getResourceAsStream(configResource)) {
            if (inputStream != null) {
                Map<String, Object> data = yaml.load(inputStream);
                if (data != null && data.containsKey("commands")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> commandsData = (List<Map<String, Object>>) data.get("commands");
                    if (commandsData != null) {
                        for (Map<String, Object> map : commandsData) {
                            configs.add(mapToConfig(map));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load commands.yaml", e);
        }

        // 2. Load auto-discovered commands from command-index.json
        try (InputStream indexStream = CommandLoader.class.getClassLoader().getResourceAsStream("command-index.json")) {
            if (indexStream != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> index = yaml.load(indexStream);
                if (index != null) {
                    for (Map.Entry<String, String> entry : index.entrySet()) {
                        String name = entry.getKey();
                        String className = entry.getValue();

                        try {
                            // Avoid duplicates if explicit config already covered this name
                            boolean exists = configs.stream().anyMatch(c -> c.name().equals(name));
                            if (!exists) {
                                // Just check if we can load the class before adding it config
                                // This ensures we don't register broken commands
                                Class<?> cls = Class.forName(className);

                                // Smart Factory Detection:
                                // Look for static methods like parse(String), valueOf(String), of(String),
                                // fromString(String)
                                // that return the class type.
                                String detectedFactory = null;
                                for (String candidate : new String[] { "parse", "valueOf", "of", "fromString" }) {
                                    java.lang.reflect.Method m = null;
                                    try {
                                        m = cls.getMethod(candidate, String.class);
                                    } catch (NoSuchMethodException e) {
                                        try {
                                            m = cls.getMethod(candidate, CharSequence.class);
                                        } catch (NoSuchMethodException e2) {
                                            // Method not found with either signature
                                        }
                                    }

                                    if (m != null &&
                                            java.lang.reflect.Modifier.isStatic(m.getModifiers()) &&
                                            m.getReturnType().isAssignableFrom(cls)) {
                                        detectedFactory = candidate;
                                        break;
                                    }
                                }

                                // Create a default HYBRID config
                                configs.add(new CommandConfig(
                                        name,
                                        className,
                                        InstanceStrategy.HYBRID,
                                        detectedFactory,
                                        "Auto-generated command for " + className,
                                        new ArrayList<>() // No examples
                                ));
                            }
                        } catch (Throwable t) {
                            // Skip classes that cannot be loaded (e.g. internal/hidden/deprecated)
                            // System.err.println("Skipping " + className + ": " + t.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // It's okay if index doesn't exist or fails, just log it if we had a logger
            System.err.println("Warning: Failed to load command-index.json: " + e.getMessage());
        }

        return configs;
    }

    private static CommandConfig mapToConfig(Map<String, Object> map) {
        String name = (String) map.get("name");
        String className = (String) map.get("className");
        InstanceStrategy strategy = InstanceStrategy.valueOf((String) map.get("strategy"));
        String factory = (String) map.get("factory");
        String description = (String) map.get("description");
        @SuppressWarnings("unchecked")
        List<String> examples = (List<String>) map.get("examples");

        return new CommandConfig(name, className, strategy, factory, description, examples);
    }
}

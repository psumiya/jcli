package jcli.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class CommandLoader {

    public static List<CommandConfig> loadCommands() {
        Yaml yaml = new Yaml(); // Safely using default constructor for simple POJOs
        String configResource = System.getProperty("jcli.config.resource", "commands.yaml");

        try (InputStream inputStream = CommandLoader.class
                .getClassLoader()
                .getResourceAsStream(configResource)) {

            if (inputStream == null) {
                throw new RuntimeException("Configuration file not found in classpath: " + configResource);
            }

            Map<String, Object> data = yaml.load(inputStream);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> commandsData = (List<Map<String, Object>>) data.get("commands");

            List<CommandConfig> configs = new ArrayList<>();
            for (Map<String, Object> map : commandsData) {
                CommandConfig config = new CommandConfig();
                config.setName((String) map.get("name"));
                config.setClassName((String) map.get("className"));
                config.setStrategy(jcli.core.InstanceStrategy.valueOf((String) map.get("strategy")));
                config.setFactory((String) map.get("factory"));
                config.setDescription((String) map.get("description"));

                @SuppressWarnings("unchecked")
                List<String> examples = (List<String>) map.get("examples");
                config.setExamples(examples);

                configs.add(config);
            }
            return configs;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load commands.yaml", e);
        }
    }
}

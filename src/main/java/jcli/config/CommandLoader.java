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
                String name = (String) map.get("name");
                String className = (String) map.get("className");
                InstanceStrategy strategy = jcli.core.InstanceStrategy.valueOf((String) map.get("strategy"));
                String factory = (String) map.get("factory");
                String description = (String) map.get("description");
                @SuppressWarnings("unchecked")
                List<String> examples = (List<String>) map.get("examples");

                configs.add(new CommandConfig(name, className, strategy, factory, description, examples));
            }
            return configs;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load commands.yaml", e);
        }
    }
}

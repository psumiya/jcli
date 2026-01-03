package jcli.config;

import jcli.core.InstanceStrategy;
import java.util.List;

public record CommandConfig(
        String name,
        String className,
        InstanceStrategy strategy,
        String factory,
        String description,
        List<String> examples) {
}

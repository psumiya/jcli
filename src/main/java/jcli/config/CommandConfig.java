package jcli.config;

import jcli.core.InstanceStrategy;

public class CommandConfig {
    public String name;
    public String className;
    public InstanceStrategy strategy;
    public String factory;
    public String description;

    // Getters/Setters for SnakeYAML if needed (SnakeYAML uses public fields or
    // getters/setters)
    // Using public fields for simplicity with standard Java Bean behavior.

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public InstanceStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(InstanceStrategy strategy) {
        this.strategy = strategy;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private java.util.List<String> examples;

    public java.util.List<String> getExamples() {
        return examples;
    }

    public void setExamples(java.util.List<String> examples) {
        this.examples = examples;
    }
}

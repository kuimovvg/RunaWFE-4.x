package ru.runa.gpd.handler;

public class HandlerArtifact extends Artifact {
    public static final String ACTION = "actionHandler";
    public static final String DECISION = "decisionHandler";
    public static final String ASSIGNMENT = "assignmentHandler";
    public static final String TASK_HANDLER = "botHandler";
    private String type;
    private String configurerClassName;

    public HandlerArtifact() {
    }

    public HandlerArtifact(HandlerArtifact artifact) {
        super(artifact);
    }

    public HandlerArtifact(boolean enabled, String name, String label, String type, String configurerClassName) {
        super(enabled, name, label);
        setType(type);
        setConfigurerClassName(configurerClassName);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfigurerClassName() {
        return configurerClassName;
    }

    public void setConfigurerClassName(String configuratorClassName) {
        this.configurerClassName = configuratorClassName;
    }
}

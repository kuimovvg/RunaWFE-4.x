package ru.runa.gpd.handler;

public class Artifact {
    private boolean enabled;
    private String name;
    private String label;

    public Artifact() {
    }

    public Artifact(Artifact artifact) {
        this.enabled = artifact.isEnabled();
        this.name = artifact.getName();
        this.label = artifact.getLabel();
    }

    public Artifact(boolean enabled, String name, String label) {
        this.enabled = enabled;
        this.name = name;
        this.label = label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

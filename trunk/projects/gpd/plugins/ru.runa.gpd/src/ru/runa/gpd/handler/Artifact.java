package ru.runa.gpd.handler;

public class Artifact {
    private boolean enabled;
    private String name;
    private String displayName;

    public Artifact() {
    }

    public Artifact(Artifact artifact) {
        this.enabled = artifact.isEnabled();
        this.name = artifact.getName();
        this.displayName = artifact.getDisplayName();
    }

    public Artifact(boolean enabled, String name, String displayName) {
        this.enabled = enabled;
        this.name = name;
        this.displayName = displayName;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

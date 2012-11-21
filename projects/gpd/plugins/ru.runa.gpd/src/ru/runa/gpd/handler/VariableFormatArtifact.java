package ru.runa.gpd.handler;

public class VariableFormatArtifact extends Artifact {
    private String variableClassName;

    public VariableFormatArtifact() {
    }

    public VariableFormatArtifact(boolean enabled, String className, String label, String variableClassName) {
        super(enabled, className, label);
        setVariableClassName(variableClassName);
    }

    public String getVariableClassName() {
        return variableClassName;
    }

    public void setVariableClassName(String variableClassName) {
        this.variableClassName = variableClassName;
    }
}

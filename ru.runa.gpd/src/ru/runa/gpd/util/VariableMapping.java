package ru.runa.gpd.util;

public class VariableMapping {
    public static final String USAGE_READ = "read";
    public static final String USAGE_WRITE = "write";
    public static final String USAGE_MULTIINSTANCE_LINK = "multiinstancelink";
    public static final String USAGE_SELECTOR = "selector";
    public static final String USAGE_TEXT = "text";
    public static final String USAGE_DISCRIMINATOR_VARIABLE = "variable";
    public static final String USAGE_DISCRIMINATOR_GROUP = "group";
    public static final String USAGE_DISCRIMINATOR_RELATION = "relation";
    
    private String processVariableName;
    private String subprocessVariableName;
    private String usage;

    public VariableMapping() {
    }

    public VariableMapping(String processVariableName, String subprocessVariableName, String usage) {
        this.processVariableName = processVariableName;
        this.subprocessVariableName = subprocessVariableName;
        this.usage = usage;
    }

    public String getProcessVariableName() {
        return processVariableName;
    }

    public void setProcessVariableName(String processVariable) {
        this.processVariableName = processVariable;
    }

    public String getSubprocessVariableName() {
        return subprocessVariableName;
    }

    public void setSubprocessVariableName(String subprocessVariable) {
        this.subprocessVariableName = subprocessVariable;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public boolean isReadable() {
        return hasUsage(USAGE_READ);
    }

    public boolean isWritable() {
        return hasUsage(USAGE_WRITE);
    }

    public boolean isMultiinstanceLink() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK);
    }

    public boolean isMultiinstanceLinkByVariable() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_VARIABLE);
    }

    public boolean isMultiinstanceLinkByGroup() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_GROUP);
    }

    public boolean isMultiinstanceLinkByRelation() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_RELATION);
    }

    public boolean isPropertySelector() {
        return hasUsage(USAGE_SELECTOR);
    }

    public boolean isText() {
        return hasUsage(USAGE_TEXT);
    }

    public boolean hasUsage(String accessLiteral) {
        if (usage == null) {
            return false;
        }
        return usage.indexOf(accessLiteral) != -1;
    }

    public VariableMapping getCopy() {
        return new VariableMapping(processVariableName, subprocessVariableName, usage);
    }
    
    @Override
    public String toString() {
        return processVariableName + "=" + subprocessVariableName + " (" + usage + ")";
    }
}

package ru.runa.gpd.util;

public class VariableMapping {
    public static final String USAGE_READ = "read";
    public static final String USAGE_WRITE = "write";
    public static final String USAGE_MULTIINSTANCE_LINK = "multiinstancelink";
    public static final String USAGE_MULTIINSTANCE_VARS = "multiinstance-vars";
    public static final String USAGE_SELECTOR = "selector";

    public static final String MULTISUBPROCESS_VARIABLE_PLACEHOLDER = "tabVariableProcessVariable";
    
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

    public VariableMapping getCopy() {
        return new VariableMapping(processVariableName, subprocessVariableName, usage);
    }
    
    @Override
    public String toString() {
        return processVariableName + "=" + subprocessVariableName + " (" + usage + ")";
    }
}

package ru.runa.gpd.util;

public class VariableMapping {

    public static final String USAGE_READ = "read";
    public static final String USAGE_WRITE = "write";
    public static final String USAGE_MULTIINSTANCE_LINK = "multiinstancelink";
    public static final String USAGE_MULTIINSTANCE_VARS = "multiinstance-vars";
    public static final String USAGE_SELECTOR = "selector";

    private String processVariableName;
    private String subprocessVariableName;
    private String usage;

    public VariableMapping() {
    }

    public VariableMapping(String processVariable, String subprocessVariable, String usage) {
        this.processVariableName = processVariable;
        this.subprocessVariableName = subprocessVariable;
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

}

package org.jbpm.ui.util;

public class VariableMapping {

    public static final String USAGE_READ = "read";
    public static final String USAGE_WRITE = "write";
    public static final String USAGE_MULTIINSTANCE_LINK = "multiinstancelink";
    public static final String USAGE_MULTIINSTANCE_VARS = "multiinstance-vars";
    public static final String USAGE_SELECTOR = "selector";

    private String processVariable;
    private String subprocessVariable;
    private String usage;

    public VariableMapping() {
    }

    public VariableMapping(String processVariable, String subprocessVariable, String usage) {
        this.processVariable = processVariable;
        this.subprocessVariable = subprocessVariable;
        this.usage = usage;
    }

    public String getProcessVariable() {
        return processVariable;
    }

    public void setProcessVariable(String processVariable) {
        this.processVariable = processVariable;
    }

    public String getSubprocessVariable() {
        return subprocessVariable;
    }

    public void setSubprocessVariable(String subprocessVariable) {
        this.subprocessVariable = subprocessVariable;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

}

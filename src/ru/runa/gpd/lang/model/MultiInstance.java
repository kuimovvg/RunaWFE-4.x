package ru.runa.gpd.lang.model;


import ru.runa.gpd.util.VariableMapping;

public class MultiInstance extends Subprocess {

    @Override
    protected void validate() {
        super.validate();
        boolean readMultiinstanceLinkExists = false;
        boolean needTest = true;
        for (VariableMapping variableMapping : variablesList) {
            if (VariableMapping.USAGE_MULTIINSTANCE_VARS.equals(variableMapping.getUsage())
                    && variableMapping.getProcessVariable().equals("typeMultiInstance")) {
                needTest = false;
            }
            if (variableMapping.getUsage().contains(VariableMapping.USAGE_MULTIINSTANCE_LINK)
                    && variableMapping.getUsage().contains(VariableMapping.USAGE_READ)
                    && !variableMapping.getUsage().contains(VariableMapping.USAGE_WRITE)) {
                readMultiinstanceLinkExists = true;
            }
        }

        if (needTest && !readMultiinstanceLinkExists) {
            addError("multiinstance.noMultiinstanceLink");
            return;
        }
    }

    @Override
    protected boolean isCompatibleTypes(String processVarFormat, String subProcessVarFormat) {
        if (processVarFormat.contains("Array")) {
            return true;
        }
        return super.isCompatibleTypes(processVarFormat, subProcessVarFormat);
    }

}

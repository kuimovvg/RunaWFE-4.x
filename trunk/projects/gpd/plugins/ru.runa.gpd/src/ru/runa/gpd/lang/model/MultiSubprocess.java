package ru.runa.gpd.lang.model;

import java.util.List;

import ru.runa.gpd.util.VariableMapping;

public class MultiSubprocess extends Subprocess implements IMultiInstancesContainer {
    @Override
    protected void validate() {
        super.validate();
        boolean readMultiinstanceLinkExists = false;
        boolean needTest = true;
        for (VariableMapping variableMapping : variableMappings) {
            if (VariableMapping.USAGE_MULTIINSTANCE_VARS.equals(variableMapping.getUsage()) && variableMapping.getProcessVariable().equals("typeMultiInstance")) {
                needTest = false;
            }
            if (variableMapping.getUsage().contains(VariableMapping.USAGE_MULTIINSTANCE_LINK) && variableMapping.getUsage().contains(VariableMapping.USAGE_READ)
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
    protected boolean isCompatibleTypes(String javaClassName1, String javaClassName2) {
        if (List.class.getName().equals(javaClassName1)) {
            return true;
        }
        return super.isCompatibleTypes(javaClassName1, javaClassName2);
    }
}

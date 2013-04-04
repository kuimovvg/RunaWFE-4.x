package ru.runa.gpd.lang.model;

import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.wfe.var.format.ListFormat;

import com.google.common.base.Objects;

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
    protected boolean isCompatibleTypes(VariableFormatArtifact artifact1, VariableFormatArtifact artifact2) {
        if (Objects.equal(ListFormat.class.getName(), artifact1.getName())) {
            return true;
        }
        return super.isCompatibleTypes(artifact1, artifact2);
    }
}

package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.util.VariableMapping;

public class SendMessageNode extends Node implements Active {
    protected List<VariableMapping> variablesList = new ArrayList<VariableMapping>();

    @Override
    protected void validate() {
        super.validate();
        for (VariableMapping variableMapping : variablesList) {
            if (VariableMapping.USAGE_SELECTOR.equals(variableMapping.getUsage())) {
                continue;
            }
            String processVarName = variableMapping.getProcessVariable();
            if (!getProcessDefinition().getVariableNames(true).contains(processVarName)) {
                addError("message.processVariableDoesNotExist", processVarName);
                continue;
            }
        }
    }

    public List<VariableMapping> getVariablesList() {
        List<VariableMapping> result = new ArrayList<VariableMapping>();
        result.addAll(variablesList);
        return result;
    }

    public void setVariablesList(List<VariableMapping> variablesList) {
        this.variablesList.clear();
        this.variablesList.addAll(variablesList);
        setDirty();
    }

    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        return super.allowLeavingTransition(target, transitions) && transitions.size() == 0;
    }
}

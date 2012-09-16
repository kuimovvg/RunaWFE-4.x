package ru.runa.bpm.ui.jpdl3.model;

import java.util.ArrayList;
import java.util.List;

import ru.runa.bpm.ui.common.model.Active;
import ru.runa.bpm.ui.common.model.DescribableNode;
import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.util.VariableMapping;

public class SendMessageNode extends DescribableNode implements Active {

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

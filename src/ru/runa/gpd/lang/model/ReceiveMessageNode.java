package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.VariableMapping;

public class ReceiveMessageNode extends Node implements Active, ITimed {
    private final List<VariableMapping> variablesList = new ArrayList<VariableMapping>();

    @Override
    public void validate(List<ValidationError> errors) {
        super.validate(errors);
        for (VariableMapping variableMapping : variablesList) {
            if (VariableMapping.USAGE_SELECTOR.equals(variableMapping.getUsage())) {
                continue;
            }
            String processVarName = variableMapping.getProcessVariableName();
            if (!getProcessDefinition().getVariableNames(true).contains(processVarName)) {
                errors.add(ValidationError.createLocalizedError(this, "message.processVariableDoesNotExist", processVarName));
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
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && (transitions.size() == 0 || (transitions.size() == 1 && getTimer() != null));
    }

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    @Override
    public String getNextTransitionName() {
        if (getTimer() != null && getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return PluginConstants.TIMER_TRANSITION_NAME;
        }
        return super.getNextTransitionName();
    }

    @Override
    public void addLeavingTransition(Transition transition) {
        if (getTimer() == null && PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
            transition.setName(getNextTransitionName());
        }
        super.addLeavingTransition(transition);
    }
}

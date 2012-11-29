package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.TimerActionPropertyDescriptor;
import ru.runa.gpd.util.TimerDuration;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.base.Objects;

public class ReceiveMessageNode extends Node implements Active, ITimed {
    private final List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
    private TimerDuration duration;
    private TimerAction timerAction;

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
        if (duration != null && duration.getVariableName() != null && !getProcessDefinition().getVariableNames(false).contains(duration.getVariableName())) {
            addError("timerState.invalidVariable");
        }
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (super.testAttribute(target, name, value)) {
            return true;
        }
        if ("timerExists".equals(name)) {
            return Objects.equal(value, String.valueOf(timerExist()));
        }
        return false;
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
        return super.allowLeavingTransition(target, transitions) && (transitions.size() == 0 || (transitions.size() == 1 && timerExist()));
    }

    @Override
    public TimerDuration getDuration() {
        return duration;
    }

    @Override
    public TimerAction getTimerAction() {
        return timerAction;
    }

    public void setDuration(TimerDuration duration) {
        this.duration = duration;
        firePropertyChange(PROPERTY_TIMER_DURATION, null, null);
    }

    @Override
    public void setDueDate(String dueDate) {
        setDuration(new TimerDuration(dueDate));
    }

    @Override
    public void setTimerAction(TimerAction timerAction) {
        if (timerAction == TimerAction.NONE) {
            timerAction = null;
        }
        TimerAction old = this.timerAction;
        this.timerAction = timerAction;
        firePropertyChange(PROPERTY_TIMER_ACTION, old, this.timerAction);
    }

    @Override
    public boolean timerExist() {
        return duration != null;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TIMER_DURATION.equals(id)) {
            return duration;
        }
        if (PROPERTY_TIMER_ACTION.equals(id)) {
            return timerAction;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TIMER_DURATION.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            setDuration((TimerDuration) value);
        } else if (PROPERTY_TIMER_ACTION.equals(id)) {
            setTimerAction((TimerAction) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        if (timerExist()) {
            list.add(new DurationPropertyDescriptor(PROPERTY_TIMER_DURATION, this));
            list.add(new TimerActionPropertyDescriptor(PROPERTY_TIMER_ACTION, Localization.getString("Timer.action"), this));
        }
        return list;
    }

    @Override
    public String getNextTransitionName() {
        if (timerExist() && getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return PluginConstants.TIMER_TRANSITION_NAME;
        }
        return super.getNextTransitionName();
    }

    @Override
    public void addLeavingTransition(Transition transition) {
        if (!timerExist() && PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
            transition.setName(getNextTransitionName());
        }
        super.addLeavingTransition(transition);
    }

    @Override
    public void createTimer() {
        if (!timerExist()) {
            setDueDate(TimerDuration.EMPTY);
            firePropertyChange(PROPERTY_TIMER, false, true);
            setDirty();
        }
    }

    @Override
    public void removeTimer() {
        if (timerExist()) {
            this.duration = null;
            firePropertyChange(PROPERTY_TIMER, true, false);
            Transition timeoutTransition = getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME);
            if (timeoutTransition != null) {
                removeLeavingTransition(timeoutTransition);
            }
            setDirty();
        }
    }
}

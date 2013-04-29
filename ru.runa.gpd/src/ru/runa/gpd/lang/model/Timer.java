package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.TimerActionPropertyDescriptor;
import ru.runa.gpd.util.Duration;

public class Timer extends Node {
    private Duration duration = new Duration();
    private TimerAction action;

    public Duration getDelay() {
        return duration;
    }

    public void setDelay(Duration duration) {
        Duration old = this.duration;
        this.duration = duration;
        firePropertyChange(PROPERTY_TIMER_DELAY, old, duration);
        if (getParent() != null) {
            getParent().firePropertyChange(PROPERTY_TIMER_DELAY, old, duration);
        }
    }

    public TimerAction getAction() {
        return action;
    }

    public void setAction(TimerAction timerAction) {
        if (timerAction == TimerAction.NONE) {
            timerAction = null;
        }
        TimerAction old = this.action;
        this.action = timerAction;
        firePropertyChange(PROPERTY_TIMER_ACTION, old, action);
    }

    @Override
    protected void validate() {
        super.validate();
        if (duration.getVariableName() != null && !getProcessDefinition().getVariableNames(false).contains(duration.getVariableName())) {
            addError("timerState.invalidVariable");
        }
        if (action != null) {
            action.validate();
        }
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new DurationPropertyDescriptor(PROPERTY_TIMER_DELAY, getProcessDefinition(), getDelay(), Localization.getString("property.duration")));
        list.add(new TimerActionPropertyDescriptor(PROPERTY_TIMER_ACTION, Localization.getString("Timer.action"), this));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TIMER_DELAY.equals(id)) {
            return duration;
        }
        if (PROPERTY_TIMER_ACTION.equals(id)) {
            return action;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TIMER_DELAY.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            setDelay((Duration) value);
        } else if (PROPERTY_TIMER_ACTION.equals(id)) {
            setAction((TimerAction) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public String getNextTransitionName() {
        return PluginConstants.TIMER_TRANSITION_NAME;
    }

    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        return transitions.size() == 0;
    }

    @Override
    public void addLeavingTransition(Transition transition) {
        super.addLeavingTransition(transition);
        transition.setName(PluginConstants.TIMER_TRANSITION_NAME);
    }
}

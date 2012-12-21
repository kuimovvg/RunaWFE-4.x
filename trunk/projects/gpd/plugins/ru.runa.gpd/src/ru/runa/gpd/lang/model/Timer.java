package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.TimerActionPropertyDescriptor;
import ru.runa.gpd.util.Delay;

public class Timer extends Node {
    private Delay delay = new Delay();
    private TimerAction action;

    public Delay getDelay() {
        return delay;
    }

    public void setDelay(Delay delay) {
        Delay old = this.delay;
        this.delay = delay;
        firePropertyChange(PROPERTY_TIMER_DELAY, old, delay);
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
        if (delay.getVariableName() != null && !getProcessDefinition().getVariableNames(false).contains(delay.getVariableName())) {
            addError("timerState.invalidVariable");
        }
        if (action != null) {
            action.validate();
        }
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new DurationPropertyDescriptor(PROPERTY_TIMER_DELAY, this));
        list.add(new TimerActionPropertyDescriptor(PROPERTY_TIMER_ACTION, Localization.getString("Timer.action"), this));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TIMER_DELAY.equals(id)) {
            return delay;
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
            setDelay((Delay) value);
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

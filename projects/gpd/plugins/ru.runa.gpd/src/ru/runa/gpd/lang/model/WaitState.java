package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.TimerActionPropertyDescriptor;
import ru.runa.gpd.util.TimerDuration;

public class WaitState extends DescribableNode implements ITimed {
    private TimerDuration duration;
    private TimerAction timerAction = null;

    public WaitState() {
        setDueDate("0 minutes");
    }

    public String getDueDate() {
        return duration.getDuration();
    }

    @Override
    public TimerDuration getDuration() {
        return duration;
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
    public boolean timerExist() {
        return true;
    }

    @Override
    public void createTimer() {
        // do nothing
    }

    @Override
    public void removeTimer() {
        // do nothing
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
    public void setTimerAction(TimerAction timerAction) {
        if (timerAction == TimerAction.NONE) {
            timerAction = null;
        }
        TimerAction old = this.timerAction;
        this.timerAction = timerAction;
        firePropertyChange(PROPERTY_TIMER_ACTION, old, this.timerAction);
    }

    @Override
    public TimerAction getTimerAction() {
        return timerAction;
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
        list.add(new DurationPropertyDescriptor(PROPERTY_TIMER_DURATION, this));
        if (timerExist()) {
            list.add(new TimerActionPropertyDescriptor(PROPERTY_TIMER_ACTION, Localization.getString("Timer.action"), this));
        }
        return list;
    }

    @Override
    public String getNextTransitionName() {
        return PluginConstants.TIMER_TRANSITION_NAME;
    }

    @Override
    protected void validate() {
        super.validate();
        if (duration.getVariableName() != null && !getProcessDefinition().getVariableNames(false).contains(duration.getVariableName())) {
            addError("timerState.invalidVariable");
        }
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

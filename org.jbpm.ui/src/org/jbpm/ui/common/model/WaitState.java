package org.jbpm.ui.common.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.properties.DurationPropertyDescriptor;
import org.jbpm.ui.properties.TimerActionPropertyDescriptor;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.TimerDuration;

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
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TIMER_DURATION.equals(id)) {
            return duration;
        }
        if (PROPERTY_TIMER_ACTION.equals(id)) {
            return timerAction;
        }
        return super.getPropertyValue(id);
    }

    public void setTimerAction(TimerAction timerAction) {
        if (timerAction == TimerAction.NONE) {
            timerAction = null;
        }
        TimerAction old = this.timerAction;
        this.timerAction = timerAction;
        firePropertyChange(PROPERTY_TIMER_ACTION, old, this.timerAction);
    }

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
            list.add(new TimerActionPropertyDescriptor(PROPERTY_TIMER_ACTION, Messages.getString("Timer.action"), this));
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

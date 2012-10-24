package org.jbpm.ui.jpdl3.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.common.model.Active;
import org.jbpm.ui.common.model.DescribableNode;
import org.jbpm.ui.common.model.ITimed;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.TimerAction;
import org.jbpm.ui.common.model.Transition;
import org.jbpm.ui.properties.DurationPropertyDescriptor;
import org.jbpm.ui.properties.TimerActionPropertyDescriptor;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.TimerDuration;
import org.jbpm.ui.util.VariableMapping;

public class ReceiveMessageNode extends DescribableNode implements Active, ITimed {

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
        if (duration != null && duration.getVariableName() != null
                && !getProcessDefinition().getVariableNames(false).contains(duration.getVariableName())) {
            addError("timerState.invalidVariable");
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
            list.add(new TimerActionPropertyDescriptor(PROPERTY_TIMER_ACTION, Messages.getString("Timer.action"), this));
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

    public void createTimer() {
        if (!timerExist()) {
            setDueDate(TimerDuration.EMPTY);
            firePropertyChange(PROPERTY_TIMER, false, true);
            setDirty();
        }
    }

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

package ru.runa.bpm.ui.jpdl3.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.State;
import ru.runa.bpm.ui.common.model.TimerAction;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.pref.PrefConstants;
import ru.runa.bpm.ui.properties.EscalationActionPropertyDescriptor;
import ru.runa.bpm.ui.properties.EscalationDurationPropertyDescriptor;
import ru.runa.bpm.ui.properties.TimeOutActionPropertyDescriptor;
import ru.runa.bpm.ui.properties.TimeOutDurationPropertyDescriptor;
import ru.runa.bpm.ui.properties.TimerActionPropertyDescriptor;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.TimerDuration;

public class TaskState extends State {
    private TimerAction timerAction = null;
    private TimerAction escalationAction = null;
    public TimerAction getEscalationAction() {
		return escalationAction;
	}

	public void setEscalationAction(TimerAction escalationAction) {
		this.escalationAction = escalationAction;
	}

	//private TimerAction timeOutAction = null;
    private boolean ignoreSubstitution;
    private boolean useEscalation = false;
    private TimerDuration escalationTime = null;
    public TimerDuration getEscalationTime() {
		return escalationTime;
	}

	public void setEscalationTime(TimerDuration escalationTime) {
		this.escalationTime = escalationTime;
		firePropertyChange(PROPERTY_ESCALATION, null, null);
	}

    public boolean isUseEscalation() {
		return useEscalation;
	}

	public void setUseEscalation(boolean useEscalation) {
		
		if (escalationAction == null || !this.useEscalation) {
			escalationAction = new TimerAction(getProcessDefinition());
			escalationAction.setDelegationClassName("ru.runa.wf.EscalationActionHandler");
			
			String org_function = DesignerPlugin.getPrefString(PrefConstants.P_ESCALATION_CONFIG);
			if (org_function==null || org_function=="") org_function = "ru.runa.af.organizationfunction.DemoChiefFunction";
			escalationAction.setDelegationConfiguration(org_function);
			
			String repeat = DesignerPlugin.getPrefString(PrefConstants.P_ESCALATION_REPEAT);
			if (repeat!=null && repeat!="" && (new TimerDuration(repeat).hasDuration())) escalationAction.setRepeat(repeat);
			
			String expirationTime = DesignerPlugin.getPrefString(PrefConstants.P_ESCALATION_DURATION);
			if (expirationTime!=null && expirationTime!="" && (new TimerDuration(expirationTime).hasDuration()))
				escalationTime = new TimerDuration(expirationTime);
			else
				escalationTime = null;
		}
		this.useEscalation = useEscalation;
		firePropertyChange(PROPERTY_ESCALATION, null, null);
	}

	/**
     * @return true if there is more than one output transitions (timer
     *         transition ignored)
     */
    public boolean hasMultipleOutputTransitions() {
        int count = getLeavingTransitions().size();
        if (hasTimeoutTransition()) {
            count--;
        }
        return count > 1;
    }

    public boolean hasTimeoutTransition() {
        for (Transition transition : getLeavingTransitions()) {
            if (PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
                return true;
            }
        }
        return false;
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

    /*public void setTimeOutAction(TimerAction timeOutAction) {
        if (timeOutAction == TimerAction.NONE) {
            timeOutAction = null;
        }
        TimerAction old = this.timeOutAction;
        this.timeOutAction = timeOutAction;
        firePropertyChange(PROPERTY_TIMEOUT_ACTION, old, this.timeOutAction);
    }*/

    public TimerAction getTimeOutAction() {
        return null;//timeOutAction;
    }

    public boolean isIgnoreSubstitution() {
        return ignoreSubstitution;
    }

    public void setIgnoreSubstitution(boolean ignoreSubstitution) {
        boolean old = this.ignoreSubstitution;
        this.ignoreSubstitution = ignoreSubstitution;
        firePropertyChange(PROPERTY_IGNORE_SUBSTITUTION, old, this.ignoreSubstitution);
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        if (timerExist() && !hasTimeoutTransition()) {
            list.add(new TimerActionPropertyDescriptor(PROPERTY_TIMER_ACTION, Messages.getString("Timer.action"), this));
        } /*else if (!timerExist()) {
            list.add(new TimeOutActionPropertyDescriptor(PROPERTY_TIMEOUT_ACTION, Messages.getString("TimeOut.action"), this));
        }*/
        list.add(new PropertyDescriptor(PROPERTY_IGNORE_SUBSTITUTION, Messages.getString("property.ignoreSubstitution")));
        list.add(new TimeOutDurationPropertyDescriptor(PROPERTY_TIMEOUT_DURATION, this));
        if (useEscalation) {
        	list.add(new EscalationActionPropertyDescriptor(PROPERTY_ESCALATION_ACTION, Messages.getString("escalation.action"), this));
        	list.add(new EscalationDurationPropertyDescriptor(PROPERTY_ESCALATION_DURATION, this));
        }
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TIMER_ACTION.equals(id)) {
            return timerAction;
        }
        if (PROPERTY_ESCALATION_DURATION.equals(id)) {
        	if (escalationTime==null || !escalationTime.hasDuration()) return "";
            return escalationTime;
        }
        if (PROPERTY_TIMEOUT_DURATION.equals(id)) {
        	TimerDuration d = getTimeOutDuration();
        	if (d==null || !d.hasDuration()) return "";
        	return d;
        }
        if (PROPERTY_ESCALATION_ACTION.equals(id)) {
        	return escalationAction;
        }
        if (PROPERTY_IGNORE_SUBSTITUTION.equals(id)) {
            return ignoreSubstitution ? Messages.getString("message.yes") : Messages.getString("message.no");
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TIMER_ACTION.equals(id)) {
            setTimerAction((TimerAction) value);
        } else if (PROPERTY_TIMEOUT_DURATION.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            setTimeOutDuration((TimerDuration) value);
        } else if (PROPERTY_ESCALATION_ACTION.equals(id)) {
            setEscalationAction((TimerAction) value);
        } else if (PROPERTY_ESCALATION_DURATION.equals(id)) {
        	setEscalationTime((TimerDuration) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}

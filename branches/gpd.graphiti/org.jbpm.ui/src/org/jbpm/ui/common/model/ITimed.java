package org.jbpm.ui.common.model;

import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.TimerDuration;

public interface ITimed {
    public static final String CURRENT_DATE_MESSAGE = Messages.getString("duration.baseDateNow");

    public boolean timerExist();

	public TimerDuration getDuration();

	public void setDueDate(String dueDate);
	
	public void setTimerAction(TimerAction timerAction);
	
	public TimerAction getTimerAction();
}

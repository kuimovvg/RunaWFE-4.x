package ru.runa.bpm.ui.common.model;

import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.TimerDuration;

public interface ITimed {
    public static final String CURRENT_DATE_MESSAGE = Messages.getString("duration.baseDateNow");

    public boolean timerExist();

	public TimerDuration getDuration();

	public void setDueDate(String dueDate);
	
	public void setTimerAction(TimerAction timerAction);
	
	public TimerAction getTimerAction();
}

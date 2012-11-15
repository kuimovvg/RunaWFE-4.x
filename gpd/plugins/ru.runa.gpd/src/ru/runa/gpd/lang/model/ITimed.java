package ru.runa.gpd.lang.model;


import ru.runa.gpd.Localization;
import ru.runa.gpd.util.TimerDuration;

public interface ITimed {
    public static final String CURRENT_DATE_MESSAGE = Localization.getString("duration.baseDateNow");

    public boolean timerExist();

	public TimerDuration getDuration();

	public void setDueDate(String dueDate);
	
	public void setTimerAction(TimerAction timerAction);
	
	public TimerAction getTimerAction();
}

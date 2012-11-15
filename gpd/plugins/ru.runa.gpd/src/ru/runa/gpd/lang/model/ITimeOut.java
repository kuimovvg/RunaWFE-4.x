package ru.runa.gpd.lang.model;

import ru.runa.gpd.util.TimerDuration;

public interface ITimeOut {
    //public boolean timeOutExist();

    public TimerDuration getTimeOutDuration();
    
    public TimerAction getTimeOutAction();

    public void setTimeOutDueDate(String timeOutDueDate);
    
    //public void setTimeOutAction(TimerAction timeOutAction);
}

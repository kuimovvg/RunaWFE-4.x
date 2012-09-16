package ru.runa.bpm.ui.common.model;

import ru.runa.bpm.ui.util.TimerDuration;

public interface ITimeOut {
    //public boolean timeOutExist();

    public TimerDuration getTimeOutDuration();
    
    public TimerAction getTimeOutAction();

    public void setTimeOutDueDate(String timeOutDueDate);
    
    //public void setTimeOutAction(TimerAction timeOutAction);
}

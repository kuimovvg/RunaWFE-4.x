package org.jbpm.ui.common.model;

import org.jbpm.ui.util.TimerDuration;

public interface ITimeOut {
    //public boolean timeOutExist();

    public TimerDuration getTimeOutDuration();
    
    public TimerAction getTimeOutAction();

    public void setTimeOutDueDate(String timeOutDueDate);
    
    //public void setTimeOutAction(TimerAction timeOutAction);
}

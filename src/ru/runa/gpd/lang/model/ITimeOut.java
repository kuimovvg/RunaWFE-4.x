package ru.runa.gpd.lang.model;

import ru.runa.gpd.util.Delay;

public interface ITimeOut {
    public Delay getTimeOutDelay();

    public void setTimeOutDelay(Delay delay);

    public TimerAction getTimeOutAction();
}

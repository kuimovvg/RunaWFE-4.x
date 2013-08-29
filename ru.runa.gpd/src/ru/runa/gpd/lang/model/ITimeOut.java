package ru.runa.gpd.lang.model;

import ru.runa.gpd.util.Duration;

public interface ITimeOut {
    public Duration getTimeOutDelay();

    public void setTimeOutDelay(Duration duration);

    public TimerAction getTimeOutAction();
}

package ru.runa.gpd.lang.model;


public class WaitState extends Node implements ITimed {
    @Override
    public Timer getTimer() {
        Timer timer = getFirstChild(Timer.class);
        if (timer == null) {
            addChild(new Timer());
        }
        return timer;
    }
}

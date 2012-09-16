package ru.runa.bpm.ui.jpdl2.model;

import java.util.List;

import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.State;
import ru.runa.bpm.ui.common.model.TimerAction;
import ru.runa.bpm.ui.common.model.Transition;

public class TimerState extends State {

    public TimerAction getTimerAction() {
        // method stub
        return null;
    }

    public void setTimerAction(TimerAction timerAction) {
        // method stub
    }

    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        if (transitions.size() == 2) {
            return false;
        }
        if (!timerExist() && transitions.size() == 1) {
            return false;
        }
        return true;
    }

    public TimerAction getTimeOutAction() {
        // Auto-generated method stub
        return null;
    }

    public void setTimeOutAction(TimerAction timeOutAction) {
        // Auto-generated method stub
    }
}

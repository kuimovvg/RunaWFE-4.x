package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;

public class DeleteTimerDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        Timer timer = ((ITimed) getSelection()).getTimer();
        timer.getParent().removeChild(timer);
    }
}

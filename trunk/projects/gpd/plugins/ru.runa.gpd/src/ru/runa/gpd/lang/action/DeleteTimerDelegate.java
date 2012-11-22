package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.ITimed;

public class DeleteTimerDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        ITimed timed = (ITimed) getSelection();
        timed.removeTimer();
    }
}

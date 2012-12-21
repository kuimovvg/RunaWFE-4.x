package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.Timer;

public class AddTimerDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        getSelection().addChild(new Timer());
    }
}

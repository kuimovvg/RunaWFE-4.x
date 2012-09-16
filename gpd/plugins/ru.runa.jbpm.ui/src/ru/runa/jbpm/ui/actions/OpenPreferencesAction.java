package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionFactory;

public class OpenPreferencesAction extends BaseActionDelegate {

    public void run(IAction action) {
        ActionFactory.PREFERENCES.create(window).run();
    }
}

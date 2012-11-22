package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.TaskState;

public class EnableEscalationOnTaskDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        TaskState state = getSelection();
        state.setUseEscalation(true);
    }
}

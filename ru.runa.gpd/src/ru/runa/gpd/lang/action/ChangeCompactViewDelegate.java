package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.State;

public class ChangeCompactViewDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        State state = getSelection();
        state.setMinimizedView(!state.isMinimizedView());
    }
}

package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.State;

public class CompactViewFeatureDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (action.isEnabled()) {
            State state = getSelection();
            action.setChecked(state.isMinimizedView());
        }
    }

    @Override
    public void run(IAction action) {
        State state = getSelection();
        state.setMinimizedView(!state.isMinimizedView());
    }
}

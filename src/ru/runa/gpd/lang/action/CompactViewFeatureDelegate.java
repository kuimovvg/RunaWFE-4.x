package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.Conjunction;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.State;

public class CompactViewFeatureDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        GraphElement element = getSelection();
        action.setEnabled(element instanceof State || element instanceof Conjunction);
        if (action.isEnabled()) {
            if (element instanceof State) {
                action.setChecked(((State) element).isMinimizedView());
            }
            if (element instanceof Conjunction) {
                action.setChecked(((Conjunction) element).isMinimizedView());
            }
        }
    }

    @Override
    public void run(IAction action) {
        GraphElement element = getSelection();
        if (element instanceof State) {
            ((State) element).setMinimizedView(!((State) element).isMinimizedView());
        }
        if (element instanceof Conjunction) {
            ((Conjunction) element).setMinimizedView(!((Conjunction) element).isMinimizedView());
        }
    }
}

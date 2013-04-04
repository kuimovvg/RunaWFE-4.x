package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.Synchronizable;

public class AsyncFeatureDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (action.isEnabled()) {
            Synchronizable synchronizable = (Synchronizable) getSelection();
            action.setChecked(synchronizable.isAsync());
        }
    }

    @Override
    public void run(IAction action) {
        Synchronizable synchronizable = (Synchronizable) getSelection();
        synchronizable.setAsync(!synchronizable.isAsync());
    }
}

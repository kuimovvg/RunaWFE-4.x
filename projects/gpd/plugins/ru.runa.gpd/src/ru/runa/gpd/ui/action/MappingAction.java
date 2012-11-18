package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.ui.dialog.MappingDialog;
import ru.runa.gpd.util.LocalizationsProvider;
import ru.runa.gpd.util.ProjectFinder;

public class MappingAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        LocalizationsProvider.INSTANCE.init();
        MappingDialog dialog = new MappingDialog(window.getShell());
        if (dialog.open() != IDialogConstants.CANCEL_ID) {
            LocalizationsProvider.INSTANCE.save();
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(ProjectFinder.getCurrentProject() != null);
    }
}

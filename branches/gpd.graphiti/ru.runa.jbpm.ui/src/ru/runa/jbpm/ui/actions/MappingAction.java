package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.jbpm.ui.dialog.MappingDialog;
import org.jbpm.ui.util.MappingContentProvider;
import org.jbpm.ui.util.ProjectFinder;

public class MappingAction extends BaseActionDelegate {

    public void run(IAction action) {
        MappingContentProvider.INSTANCE.addMappingInfo();
        MappingDialog dialog = new MappingDialog(window.getShell());
        if (dialog.open() != IDialogConstants.CANCEL_ID) {
            MappingContentProvider.INSTANCE.saveToInput();
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(ProjectFinder.getCurrentProject() != null);
    }

}

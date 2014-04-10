package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.ui.dialog.MultiTaskExecutorsDialog;

public class EditMultiTaskExecutorsAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        MultiTaskState state = getSelection();
        MultiTaskExecutorsDialog dialog = new MultiTaskExecutorsDialog(state);
        if (dialog.open() == IDialogConstants.OK_ID) {
            state.setExecutorsDiscriminatorUsage(dialog.getExecutorsDiscriminatorUsage());
            state.setExecutorsDiscriminatorValue(dialog.getExecutorsDiscriminatorValue());
        }
    }

}

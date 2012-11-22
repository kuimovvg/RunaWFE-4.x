package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import ru.runa.gpd.lang.model.MultiInstance;
import ru.runa.gpd.ui.dialog.MultiInstanceDialog;

public class MultiInstanceDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        MultiInstance multiInstance = getSelection();
        MultiInstanceDialog dialog = new MultiInstanceDialog(multiInstance);
        if (dialog.open() != Window.CANCEL) {
            multiInstance.setVariablesList(dialog.getSubprocessVariables());
            multiInstance.setSubProcessName(dialog.getSubprocessName());
        }
    }
}

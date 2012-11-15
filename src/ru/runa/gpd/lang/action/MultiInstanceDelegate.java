package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import ru.runa.gpd.lang.model.MultiInstance;
import ru.runa.gpd.ui.dialog.MultiInstanceDialog;

public class MultiInstanceDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        MultiInstance multiInstance = (MultiInstance) selectedPart.getModel();
        openDetails(multiInstance);
    }

    public void openDetails(MultiInstance multiInstance) {
        MultiInstanceDialog dialog = new MultiInstanceDialog(multiInstance);
        if (dialog.open() != Window.CANCEL) {
            multiInstance.setVariablesList(dialog.getSubprocessVariables());
            multiInstance.setSubProcessName(dialog.getSubprocessName());
        }
    }
}

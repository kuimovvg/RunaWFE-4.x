package ru.runa.bpm.ui.jpdl3.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import ru.runa.bpm.ui.common.action.BaseActionDelegate;
import ru.runa.bpm.ui.dialog.MultiInstanceDialog;
import ru.runa.bpm.ui.jpdl3.model.MultiInstance;

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

package org.jbpm.ui.jpdl3.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.jbpm.ui.common.action.BaseActionDelegate;
import org.jbpm.ui.dialog.MultiInstanceDialog;
import org.jbpm.ui.jpdl3.model.MultiInstance;

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

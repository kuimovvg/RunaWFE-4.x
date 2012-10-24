package org.jbpm.ui.jpdl3.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.jbpm.ui.common.action.BaseActionDelegate;
import org.jbpm.ui.dialog.MessageNodeDialog;
import org.jbpm.ui.jpdl3.model.SendMessageNode;

public class SendMessageConfAction extends BaseActionDelegate {

    public void run(IAction action) {
        SendMessageNode messageNode = (SendMessageNode) selectedPart.getModel();
        openDetails(messageNode);
    }

    public void openDetails(SendMessageNode messageNode) {
        MessageNodeDialog dialog = new MessageNodeDialog(messageNode.getProcessDefinition(), messageNode.getVariablesList(), true);
        if (dialog.open() != Window.CANCEL) {
            messageNode.setVariablesList(dialog.getSubprocessVariables());
        }
    }

}

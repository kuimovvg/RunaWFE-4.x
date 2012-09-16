package ru.runa.bpm.ui.jpdl3.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import ru.runa.bpm.ui.common.action.BaseActionDelegate;
import ru.runa.bpm.ui.dialog.MessageNodeDialog;
import ru.runa.bpm.ui.jpdl3.model.ReceiveMessageNode;

public class ReceiveMessageConfAction extends BaseActionDelegate {

    public void run(IAction action) {
        ReceiveMessageNode messageNode = (ReceiveMessageNode) selectedPart.getModel();
        openDetails(messageNode);
    }

    public void openDetails(ReceiveMessageNode messageNode) {
        MessageNodeDialog dialog = new MessageNodeDialog(messageNode.getProcessDefinition(), messageNode.getVariablesList(), false);
        if (dialog.open() != Window.CANCEL) {
            messageNode.setVariablesList(dialog.getSubprocessVariables());
        }
    }

}

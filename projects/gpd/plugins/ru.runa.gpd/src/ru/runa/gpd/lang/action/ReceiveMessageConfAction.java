package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import ru.runa.gpd.lang.model.ReceiveMessageNode;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;

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

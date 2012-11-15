package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import ru.runa.gpd.lang.model.SendMessageNode;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;

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

package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.part.graph.ReceiveMessageGraphicalEditPart;
import ru.runa.gpd.editor.gef.part.graph.StateGraphicalEditPart;

public class AddTimerDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        if (selectedPart instanceof StateGraphicalEditPart) {
            ((StateGraphicalEditPart) selectedPart).getModel().createTimer();
        } else if (selectedPart instanceof ReceiveMessageGraphicalEditPart) {
            ((ReceiveMessageGraphicalEditPart) selectedPart).getModel().createTimer();
        } else {
            PluginLogger.logInfo("Invalid " + selectedPart);
        }
    }

}

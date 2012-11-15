package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.part.graph.StateGraphicalEditPart;
import ru.runa.gpd.lang.model.TaskState;

public class AddEscalationDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        if (selectedPart instanceof StateGraphicalEditPart) {
        	TaskState state = (TaskState) ((StateGraphicalEditPart) selectedPart).getModel();
        	state.setUseEscalation(true);
        } else {
            PluginLogger.logInfo("Invalid " + selectedPart);
        }
    }

}

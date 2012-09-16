package ru.runa.bpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.part.graph.StateGraphicalEditPart;
import ru.runa.bpm.ui.jpdl3.model.TaskState;

public class RemoveEscalationDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        if (selectedPart instanceof StateGraphicalEditPart) {
        	TaskState state = (TaskState) ((StateGraphicalEditPart) selectedPart).getModel();
        	state.setUseEscalation(false);
        } else {
            DesignerLogger.logInfo("Invalid " + selectedPart);
        }
    }

}

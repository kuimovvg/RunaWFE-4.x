package org.jbpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.common.part.graph.StateGraphicalEditPart;
import org.jbpm.ui.jpdl3.model.TaskState;

public class AddEscalationDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        if (selectedPart instanceof StateGraphicalEditPart) {
        	TaskState state = (TaskState) ((StateGraphicalEditPart) selectedPart).getModel();
        	state.setUseEscalation(true);
        } else {
            DesignerLogger.logInfo("Invalid " + selectedPart);
        }
    }

}

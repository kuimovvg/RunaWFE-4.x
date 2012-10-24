package org.jbpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.common.part.graph.StateGraphicalEditPart;
import org.jbpm.ui.jpdl3.part.graph.ReceiveMessageGraphicalEditPart;

public class RemoveTimerDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        if (selectedPart instanceof StateGraphicalEditPart) {
            ((StateGraphicalEditPart) selectedPart).getModel().removeTimer();
        } else if (selectedPart instanceof ReceiveMessageGraphicalEditPart) {
            ((ReceiveMessageGraphicalEditPart) selectedPart).getModel().removeTimer();
        } else {
            DesignerLogger.logInfo("Invalid " + selectedPart);
        }
    }

}

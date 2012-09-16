package ru.runa.bpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.part.graph.StateGraphicalEditPart;
import ru.runa.bpm.ui.jpdl3.part.graph.ReceiveMessageGraphicalEditPart;

public class AddTimerDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        if (selectedPart instanceof StateGraphicalEditPart) {
            ((StateGraphicalEditPart) selectedPart).getModel().createTimer();
        } else if (selectedPart instanceof ReceiveMessageGraphicalEditPart) {
            ((ReceiveMessageGraphicalEditPart) selectedPart).getModel().createTimer();
        } else {
            DesignerLogger.logInfo("Invalid " + selectedPart);
        }
    }

}

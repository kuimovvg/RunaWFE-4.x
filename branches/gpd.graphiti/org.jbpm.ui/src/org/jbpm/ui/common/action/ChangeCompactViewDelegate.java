package org.jbpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import org.jbpm.ui.common.part.graph.StateGraphicalEditPart;

public class ChangeCompactViewDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        StateGraphicalEditPart part = (StateGraphicalEditPart) selectedPart;
        boolean mode = !part.getModel().isMinimizedView();
        part.getModel().setMinimizedView(mode);
    }

}

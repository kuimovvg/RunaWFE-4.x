package ru.runa.bpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.common.part.graph.StateGraphicalEditPart;

public class ChangeCompactViewDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        StateGraphicalEditPart part = (StateGraphicalEditPart) selectedPart;
        boolean mode = !part.getModel().isMinimizedView();
        part.getModel().setMinimizedView(mode);
    }

}

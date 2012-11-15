package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.editor.gef.part.graph.StateGraphicalEditPart;

public class ChangeCompactViewDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        StateGraphicalEditPart part = (StateGraphicalEditPart) selectedPart;
        boolean mode = !part.getModel().isMinimizedView();
        part.getModel().setMinimizedView(mode);
    }

}

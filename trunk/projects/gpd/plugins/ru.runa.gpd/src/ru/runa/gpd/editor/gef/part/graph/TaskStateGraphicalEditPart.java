package ru.runa.gpd.editor.gef.part.graph;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.model.Transition;

public class TaskStateGraphicalEditPart extends StateGraphicalEditPart {

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connectionEditPart) {
        if (PluginConstants.TIMER_TRANSITION_NAME.equals(((Transition) connectionEditPart.getModel()).getName())) {
            return getFigure().getTimerConnectionAnchor();
        } else {
            return getFigure().getLeavingConnectionAnchor();
        }
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        if (getModel().timerExist() && getModel().getLeavingTransitions().size()==1 && getModel().getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return getFigure().getTimerConnectionAnchor();
        } else {
            return getFigure().getLeavingConnectionAnchor();
        }
    }

}

package ru.runa.bpm.ui.jpdl2.part.graph;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.common.part.graph.StateGraphicalEditPart;

public class TimerStateGraphicalEditPart extends StateGraphicalEditPart {

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connectionEditPart) {
        if (PluginConstants.TIMER_TRANSITION_NAME.equals(((Transition) connectionEditPart.getModel()).getName())) {
            return getFigure().getTimerConnectionAnchor();
        } else {
            return getFigure().getLeavingConnectionAnchor();
        }
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request arg0) {
        if (getModel().timerExist() && getModel().getLeavingTransitions().size()==1 && getModel().getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return getFigure().getTimerConnectionAnchor();
        } else {
            return getFigure().getLeavingConnectionAnchor();
        }
    }

}

package ru.runa.bpm.ui.common.policy;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import ru.runa.bpm.ui.common.figure.TransitionFigure;

public class TransitionConnectionEndpointsEditPolicy extends ConnectionEndpointEditPolicy {

    private TransitionFigure getConnectionFigure() {
        return (TransitionFigure) ((GraphicalEditPart) getHost()).getFigure();
    }
    
    private void updateLineWidth(int lineWidth) {
        getConnectionFigure().setLineWidth(lineWidth);
    }

    @Override
    protected void addSelectionHandles() {
        super.addSelectionHandles();
        updateLineWidth(TransitionFigure.LINE_WIDTH_SELECTED);
    }

    @Override
    protected void removeSelectionHandles() {
        super.removeSelectionHandles();
        updateLineWidth(TransitionFigure.LINE_WIDTH_UNSELECTED);
    }

}

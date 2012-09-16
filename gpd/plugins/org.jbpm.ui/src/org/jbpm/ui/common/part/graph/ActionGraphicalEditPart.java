package ru.runa.bpm.ui.common.part.graph;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RoutingListener;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.DragEditPartsTracker;
import ru.runa.bpm.ui.common.ActionGraphUtils;
import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.Active;
import ru.runa.bpm.ui.common.policy.ActionComponentEditPolicy;

public class ActionGraphicalEditPart extends ElementGraphicalEditPart {

    public void propertyChange(PropertyChangeEvent evt) {
        if (PROPERTY_CLASS.equals(evt.getPropertyName()) || PROPERTY_CONFIGURATION.equals(evt.getPropertyName())) {
            updateTooltip(getFigure());
            refreshVisuals();
        }
    }

    @Override
    public void refreshVisuals() {
        super.refreshVisuals();
    }

    @Override
    protected String getTooltipMessage() {
        String tooltip = getModel().toString();
        if (getModel().getDelegationConfiguration().length() > 0) {
            tooltip += "\n" + getModel().getDelegationConfiguration();
        }
        return tooltip;
    }

    @Override
    public Action getModel() {
        return (Action) super.getModel();
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new ActionComponentEditPolicy());
    }

    @Override
    protected IFigure createFigure() {
        IFigure figure = super.createFigure();
        if (getParent() instanceof TransitionGraphicalEditPart) {
            PolylineConnection connection = (PolylineConnection) ((TransitionGraphicalEditPart) getParent()).getConnectionFigure();
            connection.addRoutingListener(new RoutingListener() {

                public void invalidate(Connection connection) {
                }

                public void postRoute(Connection connection) {
                    if (getParent() == null) {
                        return;
                    }
                    int index = ((Active) getParent().getModel()).getActions().indexOf(getModel());
                    getFigure().setLocation(
                            ActionGraphUtils.getActionFigureLocation(((TransitionGraphicalEditPart) getParent()).getConnectionFigure(), index, 0,
                                    false));
                    refreshVisuals();
                }

                public void remove(Connection connection) {
                }

                public boolean route(Connection connection) {
                    return false;
                }

                public void setConstraint(Connection connection, Object constraint) {
                }
            });
        }
        updateTooltip(figure);
        return figure;
    }

    @Override
    public DragTracker getDragTracker(Request request) {
        return new WithConnectionLayerDragEditPartsTracker(this);
    }

    static class WithConnectionLayerDragEditPartsTracker extends DragEditPartsTracker {

        public WithConnectionLayerDragEditPartsTracker(EditPart sourceEditPart) {
            super(sourceEditPart);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Collection<? extends EditPart> getExclusionSet() {
            return getOperationSet();
        }
    }
}

package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RoutingListener;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.gef.figure.TransitionFigure;
import ru.runa.gpd.editor.gef.policy.ActiveLayoutEditPolicy;
import ru.runa.gpd.editor.gef.policy.TransitionConnectionBendpointEditPolicy;
import ru.runa.gpd.editor.gef.policy.TransitionConnectionEditPolicy;
import ru.runa.gpd.editor.gef.policy.TransitionConnectionEndpointsEditPolicy;
import ru.runa.gpd.handler.HandlerRegistry;
import ru.runa.gpd.handler.decision.IDecisionProvider;
import ru.runa.gpd.lang.model.Bendpoint;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;

public class TransitionGraphicalEditPart extends AbstractConnectionEditPart implements PropertyNames, PropertyChangeListener, ActionsHost {
    @Override
    public Transition getModel() {
        return (Transition) super.getModel();
    }

    @Override
    public TransitionFigure getFigure() {
        return (TransitionFigure) super.getFigure();
    }

    @Override
    protected IFigure createFigure() {
        Transition transition = getModel();
        TransitionFigure figure = transition.getTypeDefinition().getGefEntry().createFigure(getModel().getProcessDefinition());
        figure.setRoutingConstraint(constructFigureBendpointList());
        if (transition.getSource() instanceof Decision) {
            figure.setLabelText(transition.getName());
        }
        if (transition.getSource() instanceof ITimed && transition.getName().equals(PluginConstants.TIMER_TRANSITION_NAME)) {
            ITimed state = (ITimed) transition.getSource();
            if (state.getDuration() != null) {
                figure.setLabelText(state.getDuration().toString());
            } else {
                figure.setLabelText("");
            }
        }
        boolean exclusive = getModel().getSource().isExclusive() && getModel().getSource().getLeavingTransitions().size() > 1;
        figure.setExclusive(exclusive);
        if (getModel().getSource() instanceof Decision) {
            Decision decision = (Decision) getModel().getSource();
            IDecisionProvider provider = HandlerRegistry.getProvider(decision);
            if (transition.getName().equals(provider.getDefaultTransitionName(decision))) {
                figure.setDefaultFlow(true);
            }
        }
        figure.addRoutingListener(new RoutingListener() {
            @Override
            public void invalidate(Connection connection) {
            }

            @Override
            public void postRoute(Connection connection) {
                if (!getModel().getProcessDefinition().isShowActions()) {
                    return;
                }
                getFigure().checkActionsFitInFigure();
            }

            @Override
            public void remove(Connection connection) {
            }

            @Override
            public boolean route(Connection connection) {
                return false;
            }

            @Override
            public void setConstraint(Connection connection, Object constraint) {
            }
        });
        //decorateFigure(figure);
        return figure;
    }

    private List<AbsoluteBendpoint> constructFigureBendpointList() {
        List<Bendpoint> modelBendpoints = getModel().getBendpoints();
        List<AbsoluteBendpoint> result = new ArrayList<AbsoluteBendpoint>(modelBendpoints.size());
        for (Bendpoint bendpoint : modelBendpoints) {
            result.add(new AbsoluteBendpoint(bendpoint.getX(), bendpoint.getY()));
        }
        return result;
    }

    @Override
    protected void refreshVisuals() {
        Transition transition = getModel();
        if (transition.getSource() instanceof TaskState && !PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
            String label;
            if (((TaskState) transition.getSource()).hasMultipleOutputTransitions()) {
                label = transition.getName();
            } else {
                label = "";
            }
            getFigure().setLabelText(label);
        }
        TransitionFigure f = getFigure();
        f.setRoutingConstraint(constructFigureBendpointList());
        decorateFigure(f);
    }

    private void decorateFigure(TransitionFigure f) {
        f.updateSourceDecoration();
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new TransitionConnectionEndpointsEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_ROLE, new TransitionConnectionEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new TransitionConnectionBendpointEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ActiveLayoutEditPolicy());
    }

    @Override
    public void activate() {
        if (!isActive()) {
            getModel().addPropertyChangeListener(this);
            if (getModel().getSource() instanceof ITimed) {
                getModel().getSource().addPropertyChangeListener(this);
            }
            super.activate();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<? extends Object> getModelChildren() {
        if (getModel().getProcessDefinition().isShowActions()) {
            return getModel().getActions();
        }
        return super.getModelChildren();
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            getModel().removePropertyChangeListener(this);
            if (getModel().getSource() instanceof ITimed) {
                getModel().getSource().removePropertyChangeListener(this);
            }
            super.deactivate();
        }
    }

    @Override
    public void refreshActionsVisibility(boolean visible) {
        refresh();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String messageId = evt.getPropertyName();
        if (TRANSITION_BENDPOINTS_CHANGED.equals(messageId)) {
            refreshVisuals();
        } else if (PROPERTY_NAME.equals(messageId) && evt.getSource() instanceof Transition) {
            Transition transition = getModel();
            if (transition.getSource() instanceof Decision) {
                getFigure().setLabelText(transition.getName());
                // update decision configuration
                Decision decision = (Decision) transition.getSource();
                IDecisionProvider provider = HandlerRegistry.getProvider(decision);
                provider.transitionRenamed(decision, (String) evt.getOldValue(), (String) evt.getNewValue());
            }
            if (transition.getSource() instanceof ITimed) {
                ITimed state = (ITimed) transition.getSource();
                String labelText = state.timerExist() ? state.getDuration().toString() : "";
                getFigure().setLabelText(labelText);
            }
            refreshVisuals();
        } else if (PROPERTY_TIMER_DURATION.equals(messageId)) {
            Transition transition = getModel();
            if (transition.getName().equals(PluginConstants.TIMER_TRANSITION_NAME)) {
                ITimed state = (ITimed) transition.getSource();
                getFigure().setLabelText(state.getDuration().toString());
                refreshVisuals();
            }
        } else if (NODE_CHILDS_CHANGED.equals(messageId)) {
            refreshChildren();
        }
    }
}

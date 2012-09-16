package ru.runa.bpm.ui.common.part.graph;

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
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.figure.TransitionFigure;
import ru.runa.bpm.ui.common.model.Bendpoint;
import ru.runa.bpm.ui.common.model.Decision;
import ru.runa.bpm.ui.common.model.ITimed;
import ru.runa.bpm.ui.common.model.NotificationMessages;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.common.policy.ActiveLayoutEditPolicy;
import ru.runa.bpm.ui.common.policy.TransitionConnectionBendpointEditPolicy;
import ru.runa.bpm.ui.common.policy.TransitionConnectionEditPolicy;
import ru.runa.bpm.ui.common.policy.TransitionConnectionEndpointsEditPolicy;
import ru.runa.bpm.ui.custom.CustomizationRegistry;
import ru.runa.bpm.ui.custom.IDecisionProvider;

public abstract class TransitionGraphicalEditPart extends AbstractConnectionEditPart implements NotificationMessages, PropertyChangeListener, ActionsHost {

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
        final TransitionFigure figure = transition.getTypeDefinition().createFigure(getModel().getProcessDefinition());
        figure.setRoutingConstraint(constructFigureBendpointList());
        if (transition.getSource() instanceof Decision) {
            figure.setLabelText(transition.getName());
        }
        if (transition.getSource() instanceof ITimed && transition.getName().equals(PluginConstants.TIMER_TRANSITION_NAME)) {
        	ITimed state = (ITimed) transition.getSource();
        	if (state.getDuration()!=null)
        		figure.setLabelText(state.getDuration().toString());
        	else
        		figure.setLabelText("");
        }
    	boolean exclusive = getModel().getSource().isExclusive() && getModel().getSource().getLeavingTransitions().size() > 1;
        figure.setExclusive(exclusive);
    	if (getModel().getSource() instanceof Decision) {
            Decision decision = (Decision) getModel().getSource();
            IDecisionProvider provider = CustomizationRegistry.getProvider(decision);
            if (transition.getName().equals(provider.getDefaultTransitionName(decision))) {
                figure.setDefaultFlow(true);
            }
    	}
        figure.addRoutingListener(new RoutingListener() {
            public void invalidate(Connection connection) {
            }
            public void postRoute(Connection connection) {
                if (!getModel().getProcessDefinition().isShowActions()) {
                    return;
                }
                getFigure().checkActionsFitInFigure();
            }
            public void remove(Connection connection) {
            }
            public boolean route(Connection connection) {
                return false;
            }
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

    public void refreshActionsVisibility(boolean visible) {
        refresh();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String messageId = evt.getPropertyName();
        if (TRANSITION_BENDPOINTS_CHANGED.equals(messageId)) {
            refreshVisuals();
        } else if (PROPERTY_NAME.equals(messageId) && evt.getSource() instanceof Transition) {
            Transition transition = getModel();
            if (transition.getSource() instanceof Decision) {
                getFigure().setLabelText(transition.getName());
                refreshVisuals();
                // update decision configuration
                Decision decision = (Decision) transition.getSource();
                IDecisionProvider provider = CustomizationRegistry.getProvider(decision);
                provider.transitionRenamed(decision, (String) evt.getOldValue(), (String) evt.getNewValue());
            }
            if (transition.getSource() instanceof ITimed) {
                ITimed state = (ITimed) transition.getSource();
                String labelText = state.timerExist() ? state.getDuration().toString() : "";
                getFigure().setLabelText(labelText);
                refreshVisuals();
            }
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

package ru.runa.bpm.ui.common.part.graph;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import ru.runa.bpm.ui.common.figure.NodeFigure;
import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.common.policy.NodeComponentEditPolicy;
import ru.runa.bpm.ui.common.policy.NodeGraphicalNodeEditPolicy;

public class NodeGraphicalEditPart extends ElementGraphicalEditPart implements NodeEditPart {

    @Override
    public Node getModel() {
        return (Node) super.getModel();
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeComponentEditPolicy());
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NodeGraphicalNodeEditPolicy());
    }

    @Override
    public NodeFigure getFigure() {
        return (NodeFigure) super.getFigure();
    }

    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart editPart) {
        return getFigure().getLeavingConnectionAnchor();
    }

    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart editPart) {
        return getFigure().getArrivingConnectionAnchor();
    }

    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return getFigure().getLeavingConnectionAnchor();
    }

    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return getFigure().getArrivingConnectionAnchor();
    }

    @Override
    protected List<Transition> getModelSourceConnections() {
        return getModel().getLeavingTransitions();
    }

    @Override
    protected List<Transition> getModelTargetConnections() {
        return getModel().getArrivingTransitions();
    }
    
    @Override
    protected void refreshVisuals() {
        getFigure().setBounds(getModel().getConstraint());
        getFigure().revalidate();
    }
    
    @SuppressWarnings("unchecked")
	public void propertyChange(PropertyChangeEvent evt) {
        String messageId = evt.getPropertyName();
        if (NODE_ARRIVING_TRANSITION_ADDED.equals(messageId) || NODE_ARRIVING_TRANSITION_REMOVED.equals(messageId)) {
            refreshTargetConnections();
        } else if (NODE_LEAVING_TRANSITION_ADDED.equals(messageId) || NODE_LEAVING_TRANSITION_REMOVED.equals(messageId) || PROPERTY_CONFIGURATION.equals(messageId)) {
            refreshSourceConnections();
        	boolean exclusive = getModel().isExclusive() && getModel().getLeavingTransitions().size() > 1; 
        	for (TransitionGraphicalEditPart part : (List<TransitionGraphicalEditPart>) getSourceConnections()) {
        		if (part.getFigure().setExclusive(exclusive)) {
        		    part.refreshVisuals();
        		}
    		}
        } else if (NODE_BOUNDS_RESIZED.equals(messageId)) {
            refreshVisuals();
        } else if (NODE_CHILDS_CHANGED.equals(messageId)) {
            refreshChildren();
        }
    }
}

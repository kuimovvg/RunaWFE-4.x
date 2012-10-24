package org.jbpm.ui.common.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.jbpm.ui.common.command.TransitionCreateCommand;
import org.jbpm.ui.common.command.TransitionReconnectCommand;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.Transition;

public class NodeGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {

    @Override
    protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
        Node node = getNode();
        TransitionCreateCommand command = (TransitionCreateCommand) request.getStartCommand();
        if (!node.canAddArrivingTransition(command.getSource())) {
            return null;
        }
        command.setTarget(node);
        return command;
    }

    @Override
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        Node node = getNode();
        if (!node.canAddLeavingTransition(null)) {
            return null;
        }
        TransitionCreateCommand command = new TransitionCreateCommand();
        command.setSource(node);
        request.setStartCommand(command);
        return command;
    }

    @Override
    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        Node node = getNode();
        Transition transition = getTransition(request);
        if (!node.canReconnectArrivingTransition(transition, transition.getSource())
                || !transition.getSource().canReconnectLeavingTransition(transition, node)) {
            return null;
        }
        TransitionReconnectCommand cmd = new TransitionReconnectCommand();
        cmd.setTransition(transition);
        cmd.setTarget(node);
        return cmd;
    }

    @Override
    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        Node node = getNode();
        Transition transition = getTransition(request);
        if (!node.canReconnectLeavingTransition(transition, transition.getTarget())
                || !transition.getTarget().canReconnectArrivingTransition(transition, node)) {
            return null;
        }
        TransitionReconnectCommand cmd = new TransitionReconnectCommand();
        cmd.setTransition(transition);
        cmd.setSource(node);
        return cmd;
    }

    private Transition getTransition(ReconnectRequest request) {
        return (Transition) request.getConnectionEditPart().getModel();
    }

    protected Node getNode() {
        return (Node) getHost().getModel();
    }
}

package org.jbpm.ui.common.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.jbpm.ui.common.command.NodeSetNameCommand;
import org.jbpm.ui.common.part.graph.NodeGraphicalEditPart;

public class NodeDirectEditPolicy extends DirectEditPolicy {

    @Override
    protected Command getDirectEditCommand(DirectEditRequest request) {
        String value = (String) request.getCellEditor().getValue();
        NodeGraphicalEditPart nodeGraphicalEditPart = (NodeGraphicalEditPart) getHost();
        NodeSetNameCommand command = new NodeSetNameCommand();
        command.setNode(nodeGraphicalEditPart.getModel());
        command.setName(value);
        return command;
    }

    @Override
    protected void showCurrentEditValue(DirectEditRequest request) {
    }

}

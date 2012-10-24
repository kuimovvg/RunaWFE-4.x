package org.jbpm.ui.common.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.jbpm.ui.common.command.NodeDeleteCommand;
import org.jbpm.ui.common.model.Node;

public class NodeComponentEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        NodeDeleteCommand nodeDeleteCommand = new NodeDeleteCommand();
        nodeDeleteCommand.setNode((Node) getHost().getModel());
        return nodeDeleteCommand;
    }

}

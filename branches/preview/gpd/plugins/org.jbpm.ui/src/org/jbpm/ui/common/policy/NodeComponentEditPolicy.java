package ru.runa.bpm.ui.common.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import ru.runa.bpm.ui.common.command.NodeDeleteCommand;
import ru.runa.bpm.ui.common.model.Node;

public class NodeComponentEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        NodeDeleteCommand nodeDeleteCommand = new NodeDeleteCommand();
        nodeDeleteCommand.setNode((Node) getHost().getModel());
        return nodeDeleteCommand;
    }

}

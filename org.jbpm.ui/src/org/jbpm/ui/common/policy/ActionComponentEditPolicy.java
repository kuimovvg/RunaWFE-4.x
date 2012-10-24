package org.jbpm.ui.common.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.jbpm.ui.common.command.ActionDeleteCommand;
import org.jbpm.ui.common.model.Action;

public class ActionComponentEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        ActionDeleteCommand deleteCommand = new ActionDeleteCommand();
        deleteCommand.setAction((Action) getHost().getModel());
        return deleteCommand;
    }

}

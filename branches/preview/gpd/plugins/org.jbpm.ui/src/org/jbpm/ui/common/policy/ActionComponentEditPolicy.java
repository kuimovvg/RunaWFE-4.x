package ru.runa.bpm.ui.common.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import ru.runa.bpm.ui.common.command.ActionDeleteCommand;
import ru.runa.bpm.ui.common.model.Action;

public class ActionComponentEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        ActionDeleteCommand deleteCommand = new ActionDeleteCommand();
        deleteCommand.setAction((Action) getHost().getModel());
        return deleteCommand;
    }

}

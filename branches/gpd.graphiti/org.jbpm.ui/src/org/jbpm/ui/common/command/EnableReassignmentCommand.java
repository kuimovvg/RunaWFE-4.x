package org.jbpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import org.jbpm.ui.common.model.State;

public class EnableReassignmentCommand extends Command {

    private final State state;

    public EnableReassignmentCommand(State state) {
        this.state = state;
    }

    @Override
    public void execute() {
        state.setReassignmentEnabled(!state.isReassignmentEnabled());
    }

    @Override
    public void undo() {
        state.setReassignmentEnabled(!state.isReassignmentEnabled());
    }

}

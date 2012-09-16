package ru.runa.bpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import ru.runa.bpm.ui.jpdl3.model.TaskState;

public class IgnoreSubstitutionCommand extends Command {

    private final TaskState state;

    public IgnoreSubstitutionCommand(TaskState state) {
        this.state = state;
    }

    @Override
    public void execute() {
        state.setIgnoreSubstitution(!state.isIgnoreSubstitution());
    }

    @Override
    public void undo() {
        state.setIgnoreSubstitution(!state.isIgnoreSubstitution());
    }

}

package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.TaskState;

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

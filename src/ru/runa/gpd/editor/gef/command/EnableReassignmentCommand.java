package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.TaskState;

public class EnableReassignmentCommand extends Command {
    private final TaskState taskState;

    public EnableReassignmentCommand(TaskState taskState) {
        this.taskState = taskState;
    }

    @Override
    public void execute() {
        taskState.setReassignmentEnabled(!taskState.isReassignmentEnabled());
    }

    @Override
    public void undo() {
        taskState.setReassignmentEnabled(!taskState.isReassignmentEnabled());
    }

}

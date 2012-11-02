package ru.runa.wfe.task;

import ru.runa.wfe.ApplicationException;

/**
 * Thrown when trying to complete already completed task.
 * 
 * @author Dofs
 */
public class TaskAlreadyCompletedException extends ApplicationException {
    private static final long serialVersionUID = 1L;

    public TaskAlreadyCompletedException(String taskName) {
        super(taskName);
    }

}

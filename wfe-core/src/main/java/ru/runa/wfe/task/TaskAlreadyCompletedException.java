package ru.runa.wfe.task;

import ru.runa.wfe.InternalApplicationException;

/**
 * Thrown when trying to complete already completed task.
 * 
 * @author Dofs
 */
public class TaskAlreadyCompletedException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public TaskAlreadyCompletedException(String message) {
        super(message);
    }

}

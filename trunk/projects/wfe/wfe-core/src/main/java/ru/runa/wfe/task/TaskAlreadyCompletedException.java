package ru.runa.wfe.task;

import ru.runa.wfe.WfException;

/**
 * Thrown when trying to complete already completed task.
 * 
 * @author Dofs
 */
public class TaskAlreadyCompletedException extends WfException {
    private static final long serialVersionUID = 1L;

    public TaskAlreadyCompletedException(String message) {
        super(message);
    }

}

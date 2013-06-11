package ru.runa.wfe.lang;

/**
 * Tasks synchronization modes.
 * 
 * @author dofs
 * @since 4.0
 */
public enum TaskExecutionMode {
    /**
     * proceeds execution when the first task is completed. when no tasks are
     * created on entrance of this node, execution is continued.
     */
    FIRST,
    /**
     * proceeds execution when the last task is completed. when no tasks are
     * created on entrance of this node, execution is continued.
     */
    LAST
}

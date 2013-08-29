package ru.runa.gpd.lang.model;

import ru.runa.wfe.lang.TaskExecutionMode;

public class MultiTaskState extends TaskState implements IMultiInstancesContainer {
    private String executorsVariableName;
    private TaskExecutionMode taskExecutionMode = TaskExecutionMode.LAST;

    @Override
    protected boolean isSwimlaneDisabled() {
        return true;
    }

    public String getExecutorsVariableName() {
        return executorsVariableName;
    }

    @Override
    public String getSwimlaneLabel() {
        return executorsVariableName != null ? "(" + executorsVariableName + ")" : "";
    }

    public void setExecutorsVariableName(String executorsVariableName) {
        String old = this.executorsVariableName;
        this.executorsVariableName = executorsVariableName;
        firePropertyChange(PROPERTY_SWIMLANE, old, executorsVariableName);
    }

    public TaskExecutionMode getTaskExecutionMode() {
        return taskExecutionMode;
    }

    public void setTaskExecutionMode(TaskExecutionMode taskExecutionMode) {
        TaskExecutionMode old = this.taskExecutionMode;
        this.taskExecutionMode = taskExecutionMode;
        firePropertyChange(PROPERTY_TASK_EXECUTION_MODE, old, taskExecutionMode);
    }
}

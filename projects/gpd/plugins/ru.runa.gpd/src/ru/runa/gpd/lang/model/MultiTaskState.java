package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.wfe.lang.TaskExecutionMode;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MultiTaskState extends TaskState implements IMultiInstancesContainer {
    public static final String USAGE_DEFAULT = VariableMapping.USAGE_MULTIINSTANCE_LINK + ", " + VariableMapping.USAGE_DISCRIMINATOR_VARIABLE;
    private String executorsDiscriminatorUsage = USAGE_DEFAULT;
    private String executorsDiscriminatorValue = "";
    private TaskExecutionMode taskExecutionMode = TaskExecutionMode.LAST;

    @Override
    protected boolean isSwimlaneDisabled() {
        return true;
    }

    public VariableMapping getDiscriminatorMapping() {
        return new VariableMapping(getExecutorsDiscriminatorValue(), null, getExecutorsDiscriminatorUsage());
    }

    public MultiinstanceParameters getMultiinstanceParameters() {
        VariableMapping mapping = getDiscriminatorMapping();
        return new MultiinstanceParameters(Lists.newArrayList(mapping));
    }

    public String getExecutorsDiscriminatorUsage() {
        return executorsDiscriminatorUsage;
    }
    
    public void setExecutorsDiscriminatorUsage(String executorsDiscriminatorMode) {
        String old = this.executorsDiscriminatorUsage;
        this.executorsDiscriminatorUsage = executorsDiscriminatorMode;
        firePropertyChange(PROPERTY_MULTI_TASK_EXECUTORS_MODE, old, executorsDiscriminatorMode);
    }
    
    public String getExecutorsDiscriminatorValue() {
        return executorsDiscriminatorValue;
    }
    
    public void setExecutorsDiscriminatorValue(String executorsDiscriminatorValue) {
        String old = this.executorsDiscriminatorValue;
        this.executorsDiscriminatorValue = executorsDiscriminatorValue;
        firePropertyChange(PROPERTY_SWIMLANE, old, executorsDiscriminatorValue);
    }

    @Override
    public String getSwimlaneLabel() {
        return executorsDiscriminatorValue != null ? "(" + executorsDiscriminatorValue + ")" : "";
    }

    public TaskExecutionMode getTaskExecutionMode() {
        return taskExecutionMode;
    }

    public void setTaskExecutionMode(TaskExecutionMode taskExecutionMode) {
        TaskExecutionMode old = this.taskExecutionMode;
        this.taskExecutionMode = taskExecutionMode;
        firePropertyChange(PROPERTY_TASK_EXECUTION_MODE, old, taskExecutionMode);
    }
    
    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (Strings.isNullOrEmpty(executorsDiscriminatorValue)) {
            errors.add(ValidationError.createLocalizedWarning(this, "multiTaskState.executors.discriminator.null"));
            return;
        }
    }
    
}

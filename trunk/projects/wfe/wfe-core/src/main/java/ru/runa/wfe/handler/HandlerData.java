package ru.runa.wfe.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Parameters holder for handler.
 * 
 * @author dofs
 */
@SuppressWarnings("unchecked")
public class HandlerData {
    private static final Log log = LogFactory.getLog(HandlerData.class);
    private final ParamsDef paramsDef;

    private boolean failOnError;
    private final Long processId;
    private final String taskName;
    private final IVariableProvider variableProvider;
    private final Map<String, Object> outputVariables = new HashMap<String, Object>();

    public HandlerData(ParamsDef paramsDef, ExecutionContext context) {
        this.paramsDef = paramsDef;
        processId = context.getProcess().getId();
        taskName = getClass().getSimpleName();
        variableProvider = context.getVariableProvider();
    }

    public HandlerData(ParamsDef paramsDef, IVariableProvider variableProvider, WfTask task) {
        this.paramsDef = paramsDef;
        processId = task.getProcessId();
        taskName = task.getName();
        this.variableProvider = variableProvider;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public Long getProcessId() {
        return processId;
    }

    public String getTaskName() {
        return taskName;
    }

    public Map<String, Object> getOutputVariables() {
        return outputVariables;
    }

    public <T> T getInputParam(String name) {
        return (T) paramsDef.getInputParamValueNotNull(name, variableProvider);
    }

    public <T> T getInputParam(String name, T defaultValue) {
        T result = paramsDef.getInputParamValue(name, variableProvider);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    public <T> T getInputParam(Class<T> clazz, String name) {
        Object object = getInputParam(name);
        return TypeConversionUtil.convertTo(object, clazz);
    }

    public <T> T getInputParam(Class<T> clazz, String name, T defaultValue) {
        Object object = getInputParam(name, defaultValue);
        return TypeConversionUtil.convertTo(object, clazz);
    }

    public IVariableProvider getVariableProvider() {
        return variableProvider;
    }

    public Map<String, ParamDef> getInputParams() {
        return paramsDef.getInputParams();
    }

    public Map<String, ParamDef> getOutputParams() {
        return paramsDef.getOutputParams();
    }

    public ParamDef getOutputParamNotNull(String name) {
        return paramsDef.getOutputParamNotNull(name);
    }

    public void setOutputParam(String name, Object value) {
        ParamDef paramDef = paramsDef.getOutputParam(name);
        if (paramDef == null) {
            log.warn(processId + ": Want to set " + name + "=" + value);
            return;
            // throw new NullPointerException("Output parameter " + name +
            // " not defined in configuration.");
        }
        if (paramDef.getVariableName() == null) {
            throw new NullPointerException("Variable not set for output parameter " + paramDef + " in configuration.");
        }
        setOutputVariable(paramDef.getVariableName(), value);
    }

    public void setOutputVariable(String variableName, Object value) {
        if (value == null) {
            log.warn(processId + ": Setting output variable " + variableName + " value to null.");
        }
        if (value != null) {
            outputVariables.put(variableName, value);
        }
    }
}

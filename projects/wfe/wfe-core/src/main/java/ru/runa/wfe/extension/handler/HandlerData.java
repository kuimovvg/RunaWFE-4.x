package ru.runa.wfe.extension.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
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

    private boolean failOnError = true;
    private final Long processId;
    private final String taskName;
    private final String definitionName;
    private final IVariableProvider variableProvider;
    private final Map<String, Object> outputVariables = new HashMap<String, Object>();

    public HandlerData(ParamsDef paramsDef, ExecutionContext context) {
        this.paramsDef = paramsDef;
        processId = context.getProcess().getId();
        taskName = getClass().getSimpleName();
        definitionName = context.getProcessDefinition().getName();
        variableProvider = context.getVariableProvider();
    }

    public HandlerData(ParamsDef paramsDef, IVariableProvider variableProvider, WfTask task) {
        this.paramsDef = paramsDef;
        processId = task.getProcessId();
        taskName = task.getName();
        definitionName = task.getDefinitionName();
        this.variableProvider = variableProvider;
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

    public String getDefinitionName() {
        return definitionName;
    }

    public Map<String, Object> getOutputVariables() {
        return outputVariables;
    }

    /**
     * @return input parameter value or throws exception (in case of parameter is not defined or has <code>null</code> value)
     */
    public <T> T getInputParamValueNotNull(String name) {
        return (T) paramsDef.getInputParamValueNotNull(name, variableProvider);
    }

    /**
     * @return input parameter value or default value (in case of parameter is not defined or has <code>null</code> value)
     */
    public <T> T getInputParamValue(String name, T defaultValue) {
        Object result = paramsDef.getInputParamValue(name, variableProvider);
        if (result == null) {
            return defaultValue;
        }
        return (T) result;
    }

    /**
     * @return input parameter value or <code>null</code>
     */
    public <T> T getInputParamValue(String name) {
        return (T) getInputParamValue(name, null);
    }

    /**
     * @return input parameter value casted to specified class or throws exception (in case of parameter is not defined or has <code>null</code> value)
     */
    public <T> T getInputParamValueNotNull(Class<T> clazz, String name) {
        Object object = getInputParamValueNotNull(name);
        return TypeConversionUtil.convertTo(clazz, object);
    }

    /**
     * @return input parameter value casted to specified class or default value (in case of parameter is not defined or has <code>null</code> value)
     */
    public <T> T getInputParamValue(Class<T> clazz, String name, T defaultValue) {
        Object object = getInputParamValue(name, defaultValue);
        return TypeConversionUtil.convertTo(clazz, object);
    }

    /**
     * @return input parameter value casted to specified class or <code>null</code>
     */
    public <T> T getInputParamValue(Class<T> clazz, String name) {
        return getInputParamValue(clazz, name, null);
    }

    public IVariableProvider getVariableProvider() {
        return variableProvider;
    }

    public ParamsDef getParamsDef() {
        return paramsDef;
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

    public void setOutputOptionalParam(String name, Object value) {
        ParamDef paramDef = paramsDef.getOutputParam(name);
        if (paramDef == null) {
            log.debug("Optional output parameter " + name + " does not defined in configuration.");
            return;
        }
        if (paramDef.getVariableName() == null) {
            log.warn("Variable not set for output parameter " + paramDef + " in configuration.");
            return;
        }
        setOutputVariable(paramDef.getVariableName(), value);
    }

    public void setOutputParam(String name, Object value) {
        ParamDef paramDef = paramsDef.getOutputParamNotNull(name);
        if (value == null) {
            throw new InternalApplicationException("Trying to set output parameter " + paramDef + " to null.");
        }
        setOutputVariable(paramDef.getVariableName(), value);
    }

    public void setOutputVariable(String variableName, Object value) {
        if (variableName == null) {
            throw new InternalApplicationException("Trying to set output variable with null name.");
        }
        outputVariables.put(variableName, value);
    }
}

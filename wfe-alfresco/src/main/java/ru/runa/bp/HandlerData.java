package ru.runa.bp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.bp.AlfHandler.ParamDef;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.execution.dto.WfProcess;
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
    private final Map<String, ParamDef> inputParams;
    private final Map<String, ParamDef> outputParams;

    private boolean failOnError;
    private final long processId;
    private final List<Long> processIdsHierarchy;
    private final String taskName;
    private final IVariableProvider variableProvider;
    private final Map<String, Object> outputVariables = new HashMap<String, Object>();

    public HandlerData(Map<String, ParamDef> inputParams, Map<String, ParamDef> outputParams, ExecutionContext context) {
        this.inputParams = inputParams;
        this.outputParams = outputParams;
        processId = context.getProcess().getId();
        taskName = getClass().getSimpleName();
        variableProvider = context.getVariableProvider();
        processIdsHierarchy = ProcessHierarchyUtils.getProcessIds(context.getProcess().getHierarchySubProcess());
    }

    public HandlerData(Map<String, ParamDef> inputParams, Map<String, ParamDef> outputParams, Subject subject, IVariableProvider variableProvider,
            WfTask task) {
        this.inputParams = inputParams;
        this.outputParams = outputParams;
        processId = task.getProcessId();
        taskName = task.getName();
        this.variableProvider = variableProvider;
        WfProcess process = Delegates.getExecutionService().getProcess(subject, processId);
        processIdsHierarchy = ProcessHierarchyUtils.getProcessIds(process.getHierarchySubProcess());
    }

    public List<Long> getProcessIdsHierarchy() {
        return processIdsHierarchy;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public long getProcessInstanceId() {
        return processId;
    }

    public String getTaskName() {
        return taskName;
    }

    public Map<String, Object> getOutputVariables() {
        return outputVariables;
    }

    public <T> T getInputParam(String name) {
        ParamDef paramDef = inputParams.get(name);
        if (paramDef == null) {
            throw new NullPointerException("Parameter '" + name + "' not defined in configuration.");
        }
        return (T) getInputParam(name, false);
    }

    public <T> T getInputParam(String name, T defaultValue) {
        T result = (T) getInputParam(name, true);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    private <T> T getInputParam(String name, boolean optional) {
        ParamDef paramDef = inputParams.get(name);
        if (paramDef == null) {
            if (optional) {
                return null;
            }
            throw new NullPointerException("Parameter '" + name + "' not defined in configuration.");
        }
        T result = null;
        if (paramDef.variableName != null) {
            result = (T) variableProvider.getValue(paramDef.variableName);
        }
        if (result == null && paramDef.value != null) {
            result = (T) paramDef.value;
        }
        if (result == null && !optional) {
            throw new NullPointerException("Required parameter '" + paramDef + "' resolved as null");
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

    public <T> T getInputVariable(Class<T> clazz, String name, boolean required) {
        T object = variableProvider.getValue(clazz, name);
        if (required && object == null) {
            throw new NullPointerException("Required variable '" + name + "' resolved as null");
        }
        return TypeConversionUtil.convertTo(object, clazz);
    }

    public IVariableProvider getVariableProvider() {
        return variableProvider;
    }

    public Map<String, ParamDef> getInputParams() {
        return inputParams;
    }

    public Map<String, ParamDef> getOutputParams() {
        return outputParams;
    }

    public void setOutputParam(String name, Object value) {
        ParamDef paramDef = outputParams.get(name);
        if (paramDef == null) {
            log.warn(processId + ": Want to set " + name + "=" + value);
            return;
            // throw new NullPointerException("Output parameter " + name +
            // " not defined in configuration.");
        }
        if (paramDef.variableName == null) {
            throw new NullPointerException("Variable not set for output parameter " + paramDef + " in configuration.");
        }
        setOutputVariable(paramDef.variableName, value);
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

package ru.runa.bp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.ApplicationException;
import ru.runa.bp.AlfHandler.ParamDef;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.commons.TypeConversionUtil;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;

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
    private final long processInstanceId;
    private final List<Long> processIdsHierarchy = new ArrayList<Long>();
    private final String taskName;
    private final Map<String, Object> inputVariables;
    private final Map<String, Object> outputVariables = new HashMap<String, Object>();

    public HandlerData(Map<String, ParamDef> inputParams, Map<String, ParamDef> outputParams, ExecutionContext context) {
        this.inputParams = inputParams;
        this.outputParams = outputParams;
        this.processInstanceId = context.getProcessInstance().getId();
        this.taskName = getClass().getSimpleName();
        this.inputVariables = context.getContextInstance().getVariables();
        ProcessInstance instance = context.getProcessInstance();
        while (instance != null) {
            processIdsHierarchy.add(instance.getId());
            Token st = instance.getSuperProcessToken();
            instance = st != null ? st.getProcessInstance() : null;
        }
        // Collections.reverse(processIdsHierarchy);
    }

    public HandlerData(Map<String, ParamDef> inputParams, Map<String, ParamDef> outputParams, Subject subject, TaskStub taskStub) {
        try {
            this.inputParams = inputParams;
            this.outputParams = outputParams;
            this.processInstanceId = taskStub.getProcessInstanceId();
            this.taskName = taskStub.getName();
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            inputVariables = executionService.getVariables(subject, taskStub.getId());
            processIdsHierarchy.add(taskStub.getProcessInstanceId());
            ProcessInstanceStub superProcess = executionService.getSuperProcessInstanceStub(subject, taskStub.getProcessInstanceId());
            while (superProcess != null) {
                processIdsHierarchy.add(superProcess.getId());
                superProcess = executionService.getSuperProcessInstanceStub(subject, superProcess.getId());
            }
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
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
        return processInstanceId;
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
            result = (T) inputVariables.get(paramDef.variableName);
        }
        if (result == null && paramDef.value != null) {
            result = (T) paramDef.value;
        }
        if (result == null && !optional) {
            throw new NullPointerException("Required parameter '" + paramDef + "' resolved as null, vars = " + inputVariables);
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
        Object object = inputVariables.get(name);
        if (required && object == null) {
            throw new NullPointerException("Required variable '" + name + "' resolved as null, vars = " + inputVariables);
        }
        return TypeConversionUtil.convertTo(object, clazz);
    }

    public Map<String, Object> getInputVariables() {
        return inputVariables;
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
            log.warn(processInstanceId + ": Want to set " + name + "=" + value);
            return;
            // throw new NullPointerException("Output parameter " + name + " not defined in configuration.");
        }
        if (paramDef.variableName == null) {
            throw new NullPointerException("Variable not set for output parameter " + paramDef + " in configuration.");
        }
        setOutputVariable(paramDef.variableName, value);
    }

    public void setOutputVariable(String variableName, Object value) {
        if (value == null) {
            log.warn(processInstanceId + ": Setting output variable " + variableName + " value to null.");
        }
        if (value != null) {
            outputVariables.put(variableName, value);
        }
    }
}

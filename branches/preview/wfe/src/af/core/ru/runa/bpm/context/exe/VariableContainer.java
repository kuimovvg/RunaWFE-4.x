package ru.runa.bpm.context.exe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.bpm.context.log.VariableDeleteLog;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.commons.ApplicationContextFactory;

import com.google.common.base.Preconditions;

public abstract class VariableContainer implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(VariableContainer.class);

    protected Map<String, VariableInstance<?>> variableInstances = new HashMap<String, VariableInstance<?>>();

    protected abstract VariableContainer getParentVariableContainer();

    public abstract Token getToken();

    public Object getVariable(String name) {
        if (hasLocalVariable(name)) {
            return getLocalVariable(name);
        } else {
            VariableContainer parent = getParentVariableContainer();
            if (parent != null) {
                // check upwards in the token hierarchy
                return parent.getVariable(name);
            }
        }
        return null;
    }

    public void setVariable(ExecutionContext executionContext, String name, Object value) {
        VariableContainer parent = getParentVariableContainer();
        if (hasLocalVariable(name) || parent == null) {
            setLocalVariable(executionContext, name, value);
        } else {
            // so let's action to the parent token's TokenVariableMap
            parent.setVariable(executionContext, name, value);
        }
    }

    public void deleteVariable(String name) {
        deleteLocalVariable(name);
    }

    /**
     * adds all the given variables to this variable container. It doesn't
     * remove any existing variables unless they are overwritten by the given
     * variables.
     */
    public void setVariables(ExecutionContext executionContext, Map<String, Object> variables) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            setVariable(executionContext, entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Object> getVariables() {
        Map<String, Object> variables = getLocalVariables();
        VariableContainer parent = getParentVariableContainer();
        if (parent != null) {
            variables.putAll(parent.getVariables());
        }
        return variables;
    }

    private Map<String, Object> getLocalVariables() {
        Map<String, Object> variables = new HashMap<String, Object>();
        for (Map.Entry<String, VariableInstance<?>> entry : variableInstances.entrySet()) {
            variables.put(entry.getKey(), entry.getValue().getValue());
        }
        return variables;
    }

    public boolean hasLocalVariable(String name) {
        return variableInstances.containsKey(name);
    }

    public Object getLocalVariable(String name) {
        if (hasLocalVariable(name)) {
            return getVariableInstance(name).getValue();
        }
        return null;
    }

    public void setLocalVariable(ExecutionContext executionContext, String name, Object value) {
        Preconditions.checkNotNull(name, "name is null");

        VariableInstance<?> variableInstance = getVariableInstance(name);
        // if there is already a variable instance and it doesn't support the
        // current type...
        if (variableInstance != null && !variableInstance.supports(value)) {
            // delete the old variable instance
            log.debug("variable type change. deleting '" + name + "' from '" + this + "'");
            deleteLocalVariable(name);
            variableInstance = null;
        }
        if (variableInstance == null) {
            log.debug("create variable '" + name + "' in '" + this + "' with value '" + value + "'");
            variableInstance = ApplicationContextFactory.getVariableInstanceCreator().create(getToken(), name, value);
            addLocalVariable(variableInstance);
        } else {
            log.debug("update variable '" + name + "' in '" + this + "' to value '" + value + "'");
            variableInstance.setValue(value);
        }
        variableInstance.syncWithSwimlane(executionContext);
    }

    public VariableInstance<?> getVariableInstance(String name) {
        return variableInstances.get(name);
    }

    public Map<String, VariableInstance<?>> getVariableInstances() {
        return variableInstances;
    }

    public void addLocalVariable(VariableInstance<?> variableInstance) {
        variableInstances.put(variableInstance.getName(), variableInstance);
    }

    public void deleteLocalVariable(String name) {
        VariableInstance<?> variableInstance = variableInstances.remove(name);
        if (variableInstance != null) {
            getToken().addLog(new VariableDeleteLog(variableInstance));
            variableInstance.removeReferences();
        }
    }

    public ContextInstance getContextInstance() {
        return getToken().getProcessInstance().getContextInstance();
    }

    public void setVariableInstances(Map<String, VariableInstance<?>> variableInstances) {
        this.variableInstances = variableInstances;
    }
}

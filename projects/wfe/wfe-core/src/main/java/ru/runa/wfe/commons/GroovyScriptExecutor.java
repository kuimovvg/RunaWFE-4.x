package ru.runa.wfe.commons;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.GroovyExceptionInterface;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableUserType;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class GroovyScriptExecutor implements IScriptExecutor {
    protected static final Log log = LogFactory.getLog(GroovyScriptExecutor.class);
    
    @Override
    public Map<String, Object> executeScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script) {
        try {
            GroovyScriptBinding binding = createBinding(processDefinition, variableProvider);
            GroovyShell shell = new GroovyShell(binding);
            shell.evaluate(script);
            return binding.getAdjustedVariables();
        } catch (Exception e) {
            log.error("Groovy execution failed, script=" + script, e);
            if (e instanceof GroovyExceptionInterface) {
                throw new InternalApplicationException(e.getMessage());
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Object evaluateScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script) {
        try {
            GroovyScriptBinding binding = createBinding(processDefinition, variableProvider);
            GroovyShell shell = new GroovyShell(binding);
            return shell.evaluate(script);
        } catch (Exception e) {
            log.error("Groovy evaluation failed, script=" + script, e);
            if (e instanceof GroovyExceptionInterface) {
                throw new InternalApplicationException(e.getMessage());
            }
            throw Throwables.propagate(e);
        }
    }

    protected GroovyScriptBinding createBinding(ProcessDefinition processDefinition, IVariableProvider variableProvider) {
        return new GroovyScriptBinding(processDefinition, variableProvider);
    }

    public static class GroovyScriptBinding extends Binding {
        private final IVariableProvider variableProvider;
        private final Map<String, String> variableScriptingNameToNameMap = Maps.newHashMap();

        public GroovyScriptBinding(ProcessDefinition processDefinition, IVariableProvider variableProvider) {
            this.variableProvider = variableProvider;
            if (processDefinition != null) {
                for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
                    fillConversionMap(variableDefinition.getName(), variableDefinition.getScriptingName(), variableDefinition.getUserType());
                }
                for (SwimlaneDefinition swimlaneDefinition : processDefinition.getSwimlanes().values()) {
                    variableScriptingNameToNameMap.put(swimlaneDefinition.getScriptingName(), swimlaneDefinition.getName());
                }
            }
        }
        
        private void fillConversionMap(String name, String scriptingName, VariableUserType userType) {
            variableScriptingNameToNameMap.put(scriptingName, name);
            if (userType != null) {
                for (VariableDefinition attributeDefinition : userType.getAttributes()) {
                    String fullScriptingName = scriptingName + VariableUserType.DELIM + attributeDefinition.getScriptingName();
                    String fullName = name + VariableUserType.DELIM + attributeDefinition.getName();
                    fillConversionMap(fullName, fullScriptingName, attributeDefinition.getUserType());
                }
            }
        }

        @Override
        public Object getVariable(String name) {
            try {
                return super.getVariable(name);
            } catch (MissingPropertyException e) {
                return getVariableFromProcess(name);
            }
        }
        
        private String getVariableNameByScriptingName(String name) {
            String variableName = variableScriptingNameToNameMap.get(name);
            if (variableName == null) {
                log.warn("No variable name found by scripting name '" + name + "'");
                return name;
            }
            return variableName;
        }

        protected Object getVariableFromProcess(String name) {
            name = getVariableNameByScriptingName(name);
            Object value = variableProvider.getValue(name);
            if (value == null) {
                log.warn("Variable '" + name + "' passed to script as null (not defined in process)");
            }
            return value;
        }

        @Override
        public boolean hasVariable(String name) {
            throw new UnsupportedOperationException("Implement if will be used");
        }

        public Map<String, Object> getAdjustedVariables() {
            Map<String, Object> scriptingVariables = getVariables();
            Map<String, Object> result = Maps.newHashMapWithExpectedSize(scriptingVariables.size());
            for (Map.Entry<String, Object> scriptingEntry : scriptingVariables.entrySet()) {
                String variableName = getVariableNameByScriptingName(scriptingEntry.getKey());
                result.put(variableName, scriptingEntry.getValue());
            }
            return result;
        }
    }

}

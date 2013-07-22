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

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class GroovyScriptExecutor implements IScriptExecutor {
    private static final Log log = LogFactory.getLog(GroovyScriptExecutor.class);

    @Override
    public Map<String, Object> executeScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script) {
        try {
            Binding binding = createBinding(processDefinition, variableProvider);
            GroovyShell shell = new GroovyShell(binding);
            shell.evaluate(script);
            return adjustVariables(processDefinition, binding.getVariables());
        } catch (Exception e) {
            log.error("Groovy execution failed, script=" + script, e);
            if (e instanceof GroovyExceptionInterface) {
                throw new InternalApplicationException(e.getMessage());
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public <T extends Object> T evaluateScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script) {
        try {
            Binding binding = createBinding(processDefinition, variableProvider);
            GroovyShell shell = new GroovyShell(binding);
            return (T) shell.evaluate(script);
        } catch (Exception e) {
            log.error("Groovy evaluation failed, script=" + script, e);
            if (e instanceof GroovyExceptionInterface) {
                throw new InternalApplicationException(e.getMessage());
            }
            throw Throwables.propagate(e);
        }
    }

    protected Binding createBinding(ProcessDefinition processDefinition, IVariableProvider variableProvider) {
        return new GroovyScriptBinding(processDefinition, variableProvider);
    }

    public static class GroovyScriptBinding extends Binding {
        protected final ProcessDefinition processDefinition;
        protected final IVariableProvider variableProvider;

        public GroovyScriptBinding(ProcessDefinition processDefinition, IVariableProvider variableProvider) {
            this.processDefinition = processDefinition;
            this.variableProvider = variableProvider;
        }

        @Override
        public Object getVariable(String name) {
            try {
                return super.getVariable(name);
            } catch (MissingPropertyException e) {
                return getVariableFromProcess(name);
            }
        }

        protected Object getVariableFromProcess(String name) {
            if (processDefinition != null) {
                boolean found = false;
                for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
                    if (Objects.equal(name, variableDefinition.getScriptingName())) {
                        name = variableDefinition.getName();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (SwimlaneDefinition swimlaneDefinition : processDefinition.getSwimlanes().values()) {
                        if (Objects.equal(name, swimlaneDefinition.getScriptingName())) {
                            name = swimlaneDefinition.getName();
                            found = true;
                            break;
                        }
                    }
                }
            }
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

    }

    private Map<String, Object> adjustVariables(ProcessDefinition processDefinition, Map<String, Object> map) {
        Map<String, Object> result = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String variableName = entry.getKey();
            boolean found = false;
            for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
                if (Objects.equal(variableName, variableDefinition.getScriptingName())) {
                    variableName = variableDefinition.getName();
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (SwimlaneDefinition swimlaneDefinition : processDefinition.getSwimlanes().values()) {
                    if (Objects.equal(variableName, swimlaneDefinition.getScriptingName())) {
                        variableName = swimlaneDefinition.getName();
                        found = true;
                        break;
                    }
                }
            }
            result.put(variableName, entry.getValue());
        }
        return result;
    }
}

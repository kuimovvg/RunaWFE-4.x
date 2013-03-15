package ru.runa.wfe.commons;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.GroovyExceptionInterface;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Throwables;

@SuppressWarnings("unchecked")
public class GroovyScriptExecutor implements IScriptExecutor {
    private static final Log log = LogFactory.getLog(GroovyScriptExecutor.class);

    @Override
    public Map<String, Object> executeScript(String script, IVariableProvider variableProvider) {
        try {
            Binding binding = createBinding(variableProvider);
            GroovyShell shell = new GroovyShell(binding);
            shell.evaluate(script);
            return binding.getVariables();
        } catch (Exception e) {
            if (e instanceof GroovyExceptionInterface) {
                log.error("Groovy", e);
                throw new InternalApplicationException(e.getMessage());
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public <T extends Object> T evaluateScript(String script, IVariableProvider variableProvider) {
        try {
            Binding binding = createBinding(variableProvider);
            GroovyShell shell = new GroovyShell(binding);
            return (T) shell.evaluate(script);
        } catch (Exception e) {
            if (e instanceof GroovyExceptionInterface) {
                log.error("Groovy", e);
                throw new InternalApplicationException(e.getMessage());
            }
            throw Throwables.propagate(e);
        }
    }

    protected Binding createBinding(IVariableProvider variableProvider) {
        return new CustomBinding(variableProvider);
    }

    private static class CustomBinding extends Binding {
        private final IVariableProvider variableProvider;

        public CustomBinding(IVariableProvider variableProvider) {
            this.variableProvider = variableProvider;
        }

        @Override
        public Object getVariable(String name) {
            try {
                return super.getVariable(name);
            } catch (MissingPropertyException e) {
                Object value = variableProvider.getValue(name);
                if (value == null) {
                    log.warn("Variable '" + name + "' passed to script as null (not defined in process)");
                }
                return value;
            }
        }

        @Override
        public boolean hasVariable(String name) {
            throw new UnsupportedOperationException("Implement if will be used");
        }

    }

}

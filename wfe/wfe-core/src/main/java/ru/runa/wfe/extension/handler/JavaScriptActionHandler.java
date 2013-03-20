package ru.runa.wfe.extension.handler;

import java.util.HashMap;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Objects;

public class JavaScriptActionHandler implements ActionHandler {
    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public void execute(ExecutionContext executionContext) throws ScriptException {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        Bindings bindings = engine.createBindings();
        for (VariableDefinition definition : executionContext.getProcessDefinition().getVariables()) {
            Object value = executionContext.getVariable(definition.getName());
            if (value != null) {
                bindings.put(definition.getName(), value);
            }
        }
        // engine.getContext().
        // org.mozilla.javascript.NativeDate
        // NativeDate
        engine.eval(configuration, bindings);
        for (VariableDefinition definition : executionContext.getProcessDefinition().getVariables()) {
            Object value = bindings.get(definition.getName());
            Object currentValue = executionContext.getVariable(definition.getName());
            if (!Objects.equal(value, currentValue)) {
                executionContext.setVariable(definition.getName(), value);
            }
        }
    }

    private static class ProcessBindings extends HashMap<String, Object> implements Bindings {
        private static final long serialVersionUID = 1L;
        private final ExecutionContext executionContext;
        private final Bindings bindingsDelegate;

        public ProcessBindings(ExecutionContext executionContext, Bindings bindingsDelegate) {
            this.executionContext = executionContext;
            this.bindingsDelegate = bindingsDelegate;
        }

        private Object getVariableValue(String name) {
            return executionContext.getVariable(name);
        }

        @Override
        public Object get(Object key) {
            Object object = bindingsDelegate.get(key);
            if (object == null) {
                object = getVariableValue((String) key);
            }
            return object;
        }

        @Override
        public boolean containsKey(Object key) {
            if (bindingsDelegate.containsKey(key)) {
                return true;
            }
            return getVariableValue((String) key) != null;
        }

        @Override
        public Object put(String key, Object value) {
            Object object = bindingsDelegate.put(key, value);
            super.put(key, value);
            return object;
        }

        @Override
        public Object remove(Object key) {
            Object object = bindingsDelegate.remove(key);
            super.remove(key);
            return object;
        }
    }

}

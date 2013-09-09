package ru.runa.wfe.extension.handler;

import javax.script.Bindings;
import javax.script.ScriptContext;
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
        for (VariableDefinition definition : executionContext.getProcessDefinition().getVariables()) {
            Object value = executionContext.getVariable(definition.getName());
            if (value != null) {
                engine.put(definition.getScriptingName(), value);
            }
        }
        engine.eval(configuration);
        Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        for (VariableDefinition definition : executionContext.getProcessDefinition().getVariables()) {
            Object value = bindings.get(definition.getScriptingName());
            Object currentValue = executionContext.getVariable(definition.getName());
            if (!Objects.equal(value, currentValue)) {
                executionContext.setVariable(definition.getName(), value);
            }
        }
    }

}

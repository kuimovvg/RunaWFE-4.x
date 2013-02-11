package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.IScriptExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

public class GroovyActionHandler implements ActionHandler {
    private String configuration;

    public String getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    protected IScriptExecutor getScriptExecutor() {
        return new GroovyScriptExecutor();
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Map<String, Object> outVariables = getScriptExecutor().executeScript(configuration, executionContext.getVariableProvider());
        executionContext.setVariables(outVariables);
    }

}

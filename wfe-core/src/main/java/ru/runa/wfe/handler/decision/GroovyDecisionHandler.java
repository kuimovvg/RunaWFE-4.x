package ru.runa.wfe.handler.decision;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.IScriptExecutor;
import ru.runa.wfe.execution.ExecutionContext;

public class GroovyDecisionHandler implements DecisionHandler {
    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    protected IScriptExecutor getScriptExecutor() {
        return new GroovyScriptExecutor();
    }

    @Override
    public String decide(ExecutionContext executionContext) throws Exception {
        return getScriptExecutor().evaluateScript(configuration, executionContext.getVariableProvider());
    }

}

package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.IScriptExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;

public class GroovyActionHandler extends ActionHandlerBase {

    protected IScriptExecutor getScriptExecutor() {
        return new GroovyScriptExecutor();
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Map<String, Object> outVariables = getScriptExecutor().executeScript(executionContext, configuration);
        executionContext.setVariableValues(outVariables);
    }

}

package ru.runa.wfe.handler;

import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.handler.action.ActionHandler;
import ru.runa.wfe.handler.bot.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

public abstract class CommonHandler extends TaskHandlerBase implements ActionHandler {

    protected abstract Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception;

    @Override
    public void execute(ExecutionContext context) throws Exception {
        Map<String, Object> result = executeAction(context.getVariableProvider());
        context.setVariables(result);
    }

    @Override
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask task) throws Exception {
        return executeAction(variableProvider);
    }

}

package ru.runa.wf.office.shared;

import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wf.logic.bot.TaskHandler;
import ru.runa.wf.logic.bot.TaskHandlerException;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.handler.action.ActionHandler;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

public abstract class CommonHandler implements ActionHandler, TaskHandler {

    protected abstract Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception;

    public abstract void setConfiguration(String configuration) throws ConfigurationException;

    @Override
    public final void configure(byte[] config) throws TaskHandlerException {
        setConfiguration(new String(config, Charsets.UTF_8));
    }

    @Override
    public void configure(String config) throws TaskHandlerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(ExecutionContext context) {
        try {
            Map<String, Object> result = executeAction(context.getVariableProvider());
            context.setVariables(result);
        } catch (Throwable th) {
            Throwables.propagate(th);
        }
    }

    @Override
    public void handle(Subject subject, IVariableProvider variableProvider, WfTask task) throws TaskHandlerException {
        try {
            Map<String, Object> result = executeAction(variableProvider);
            DelegateFactory.getExecutionService().completeTask(subject, task.getId(), result);
        } catch (Throwable e) {
            Throwables.propagateIfPossible(e, TaskHandlerException.class);
            Throwables.propagate(e);
        }
    }

}

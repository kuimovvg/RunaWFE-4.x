package ru.runa.wfe.handler;

import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.handler.action.ActionHandler;
import ru.runa.wfe.handler.bot.TaskHandler;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

public abstract class CommonHandler implements ActionHandler, TaskHandler {
    protected abstract Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception;

    private String configuration;

    @Override
    public abstract void setConfiguration(String configuration) throws ConfigurationException;

    @Override
    public final void setConfiguration(byte[] config) {
        this.configuration = new String(config, Charsets.UTF_8);
        setConfiguration(configuration);
    }

    @Override
    public Object getConfiguration() {
        return configuration;
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
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask task) {
        try {
            return executeAction(variableProvider);
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, RuntimeException.class);
            throw new InternalApplicationException(e);
        }
    }

}

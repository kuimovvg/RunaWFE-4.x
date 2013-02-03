package ru.runa.wfe.handler;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.handler.action.ActionHandler;
import ru.runa.wfe.handler.bot.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Base class for standard XML parameter-based configuration.
 * 
 * @author dofs[197@gmail.com]
 */
public abstract class CommonParamBasedHandler extends TaskHandlerBase implements ActionHandler {
    protected Log log = LogFactory.getLog(getClass());
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        paramsDef = ParamsDef.parse(configuration);
    }

    protected abstract void executeAction(HandlerData handlerData) throws Exception;

    @Override
    public void execute(ExecutionContext context) throws Exception {
        final HandlerData handlerData = new HandlerData(paramsDef, context);
        handlerData.setFailOnError(false);
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        try {
            timeMeasurer.jobStarted();
            executeAction(handlerData);
            context.setVariables(handlerData.getOutputVariables());
            timeMeasurer.jobEnded(handlerData.getTaskName());
        } catch (Throwable th) {
            log.error("action handler execution error.", th);
            if (handlerData.isFailOnError()) {
                if (th instanceof Exception) {
                    throw (Exception) th;
                }
                throw new RuntimeException(th);
            }
        }
    }

    @Override
    public Map<String, Object> handle(final User user, final IVariableProvider variableProvider, final WfTask task) throws Exception {
        HandlerData handlerData = new HandlerData(paramsDef, variableProvider, task);
        handlerData.setFailOnError(true);
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        try {
            timeMeasurer.jobStarted();
            executeAction(handlerData);
            timeMeasurer.jobEnded("Execution of " + handlerData.getTaskName());
        } catch (Throwable th) {
            log.error("task handler execution error.", th);
            if (handlerData.isFailOnError()) {
                if (th instanceof Exception) {
                    throw (Exception) th;
                }
                throw new Exception(th);
            }
        }
        return handlerData.getOutputVariables();
    }

}

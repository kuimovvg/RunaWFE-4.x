package ru.runa.bp;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Throwables;

/**
 * Base class for RunaWFE action handler and task handler.
 * 
 * @author dofs
 */
public abstract class AlfHandler extends TaskHandlerBase implements ActionHandler {
    protected Log log = LogFactory.getLog(getClass());
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        paramsDef = ParamsDef.parse(configuration);
    }

    /**
     * Do work in Alfresco.
     * 
     * @param session
     *            alfresco connection
     * @param alfHandlerData
     *            parsed handler configuration
     * @throws Exception
     *             if error occurs
     */
    protected abstract void executeAction(AlfSession session, AlfHandlerData alfHandlerData) throws Exception;

    /**
     * Do rollback in Alfresco on transaction rollback.
     * 
     * @param session
     *            alfresco connection
     * @param alfHandlerData
     *            parsed handler configuration
     * @throws Exception
     *             if error occurs TODO unsed yet in 4.x (move to wfe bots ?)
     */
    protected void onRollback(AlfSession session, AlfHandlerData alfHandlerData) throws Exception {
        log.debug("onRollback in " + alfHandlerData.getProcessId());
    }

    @Override
    public void onRollback(final User user, final IVariableProvider variableProvider, final WfTask task) throws Exception {
        new AlfSessionWrapper<Object>() {
            @Override
            protected Object code() throws Exception {
                AlfHandlerData alfHandlerData = new AlfHandlerData(paramsDef, user, variableProvider, task);
                onRollback(session, alfHandlerData);
                return null;
            }
        }.runInSession();
    }

    @Override
    public void execute(ExecutionContext context) throws Exception {
        final AlfHandlerData handlerData = new AlfHandlerData(paramsDef, context);
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        try {
            timeMeasurer.jobStarted();
            new AlfSessionWrapper<Object>() {
                @Override
                protected Object code() throws Exception {
                    executeAction(session, handlerData);
                    return null;
                }
            }.runInSession();
            context.setVariables(handlerData.getOutputVariables());
            timeMeasurer.jobEnded(handlerData.getTaskName());
        } catch (Throwable th) {
            if (handlerData.isFailOnError()) {
                throw Throwables.propagate(th);
            }
            log.error("Alfresco action handler execution error.", th);
        }
    }

    @Override
    public Map<String, Object> handle(final User user, final IVariableProvider variableProvider, final WfTask task) throws Exception {
        return new AlfSessionWrapper<Map<String, Object>>() {
            @Override
            protected Map<String, Object> code() throws Exception {
                AlfHandlerData handlerData = new AlfHandlerData(paramsDef, user, variableProvider, task);
                TimeMeasurer timeMeasurer = new TimeMeasurer(log);
                try {
                    timeMeasurer.jobStarted();
                    executeAction(session, handlerData);
                    timeMeasurer.jobEnded("Execution of " + handlerData.getTaskName());
                } catch (Throwable th) {
                    if (handlerData.isFailOnError()) {
                        throw Throwables.propagate(th);
                    }
                    log.error("Alfresco task handler execution error.", th);
                }
                return handlerData.getOutputVariables();
            }
        }.runInSession();
    }

}

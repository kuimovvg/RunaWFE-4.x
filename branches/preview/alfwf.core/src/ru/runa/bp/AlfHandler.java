package ru.runa.bp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ru.runa.ConfigurationException;
import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.commons.TimeMeasurer;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.logic.bot.TaskHandler;
import ru.runa.wf.logic.bot.TaskHandlerException;
import ru.runa.wf.service.ExecutionService;

import com.google.common.base.Charsets;

/**
 * Base class for RunaWFE action handler and task handler.
 * 
 * @author dofs
 */
public abstract class AlfHandler implements ActionHandler, TaskHandler {
    protected Log log = LogFactory.getLog(getClass());

    /**
     * Cached configuration and extracted parameters from it.
     */
    protected String configuration;
    private final Map<String, ParamDef> inputParams = new HashMap<String, ParamDef>();
    private final Map<String, ParamDef> outputParams = new HashMap<String, ParamDef>();

    /**
     * Do work in Alfresco.
     * 
     * @param session
     *            alfresco connection
     * @param handlerData
     *            parsed handler configuration
     * @throws Exception
     *             if error occurs
     */
    protected abstract void executeAction(AlfSession session, HandlerData handlerData) throws Exception;

    /**
     * Do rollback in Alfresco on transaction rollback.
     * 
     * @param session
     *            alfresco connection
     * @param handlerData
     *            parsed handler configuration
     * @throws Exception
     *             if error occurs
     */
    protected void compensateAction(AlfSession session, HandlerData handlerData) throws Exception {
        log.debug("onRollback in " + handlerData.getProcessInstanceId());
    }

    /**
     * Action handler implementation
     */
    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        final HandlerData handlerData = new HandlerData(inputParams, outputParams, executionContext);
        handlerData.setFailOnError(false);
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
            executionContext.getContextInstance().setVariables(executionContext, handlerData.getOutputVariables());
            timeMeasurer.jobEnded(handlerData.getTaskName());
        } catch (Throwable th) {
            log.error("Alfresco action handler execution error.", th);
            if (handlerData.isFailOnError()) {
                if (th instanceof Exception) {
                    throw (Exception) th;
                }
                throw new RuntimeException(th);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setConfiguration(String configuration) throws ConfigurationException {
        this.configuration = configuration;
        if (configuration.trim().length() == 0) {
            return;
        }
        try {
            Document doc = DocumentHelper.parseText(configuration);
            Element inputElement = doc.getRootElement().element("input");
            if (inputElement != null) {
                List<Element> inputParamElements = inputElement.elements("param");
                for (Element element : inputParamElements) {
                    ParamDef paramDef = new ParamDef(element);
                    inputParams.put(paramDef.name, paramDef);
                }
            }
            Element outputElement = doc.getRootElement().element("output");
            if (outputElement != null) {
                List<Element> outputParamElements = outputElement.elements("param");
                for (Element element : outputParamElements) {
                    ParamDef paramDef = new ParamDef(element);
                    outputParams.put(paramDef.name, paramDef);
                }
            }
        } catch (Throwable th) {
            log.error("Alfresco handler configuration error.", th);
            throw new ConfigurationException(th);
        }
    }

    @Override
    public final void configure(byte[] config) throws TaskHandlerException {
        try {
            setConfiguration(new String(config, Charsets.UTF_8));
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    @Override
    public void configure(String config) throws TaskHandlerException {
        throw new UnsupportedOperationException();
    }

    /**
     * Task handler implementation
     */
    @Override
    public void handle(final Subject subject, final TaskStub taskStub) throws TaskHandlerException {
        new AlfSessionWrapper<Object>() {
            @Override
            protected Object code() throws Exception {
                HandlerData handlerData = new HandlerData(inputParams, outputParams, subject, taskStub);
                handlerData.setFailOnError(true);
                TimeMeasurer timeMeasurer = new TimeMeasurer(log);
                try {
                    timeMeasurer.jobStarted();
                    executeAction(session, handlerData);
                    timeMeasurer.jobEnded("Execution of " + handlerData.getTaskName());
                } catch (Throwable th) {
                    log.error("Alfresco task handler execution error.", th);
                    if (handlerData.isFailOnError()) {
                        if (th instanceof Exception) {
                            throw (Exception) th;
                        }
                        throw new Exception(th);
                    }
                }
                try {
                    timeMeasurer.jobStarted();
                    ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
                    executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(),
                            handlerData.getOutputVariables());
                    timeMeasurer.jobEnded("Commiting of " + handlerData.getTaskName());
                } catch (Throwable e) {
                    compensateAction(session, handlerData);
                    throw new TaskHandlerException("Unable to commit", e);
                }
                return null;
            }
        }.runInSession();
    }

    public static class ParamDef {
        public final String name;
        public final String variableName;
        public final String value;

        public ParamDef(Element element) {
            this.name = element.attributeValue("name");
            this.variableName = element.attributeValue("variable");
            this.value = element.attributeValue("value");
        }

        public String getVariableName() {
            return variableName;
        }

        @Override
        public String toString() {
            StringBuffer b = new StringBuffer(name);
            if (variableName != null) {
                b.append(" [var=").append(variableName).append("]");
            }
            if (value != null) {
                b.append(" [value=").append(value).append("]");
            }
            return b.toString();
        }
    }

}

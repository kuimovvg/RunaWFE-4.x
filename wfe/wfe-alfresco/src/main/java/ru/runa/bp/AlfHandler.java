package ru.runa.bp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.codec.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.handler.action.ActionHandler;
import ru.runa.wfe.handler.bot.TaskHandler;
import ru.runa.wfe.handler.bot.TaskHandlerException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

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
     *             if error occurs TODO unsed yet in 4.x (move to wfe bots ?)
     */
    protected void compensateAction(AlfSession session, HandlerData handlerData) throws Exception {
        log.debug("onRollback in " + handlerData.getProcessInstanceId());
    }

    /**
     * Action handler implementation
     */
    @Override
    public void execute(ExecutionContext context) throws Exception {
        final HandlerData handlerData = new HandlerData(inputParams, outputParams, context);
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
            context.setVariables(handlerData.getOutputVariables());
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
    public void setConfiguration(byte[] configuration) throws Exception {
        setConfiguration(new String(configuration, Charsets.UTF_8));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setConfiguration(String configuration) throws ConfigurationException {
        this.configuration = configuration;
        if (configuration.trim().length() == 0) {
            return;
        }
        try {
            Document doc = XmlUtils.parseWithoutValidation(configuration);
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

    public final void configure(byte[] config) throws TaskHandlerException {
        try {
            setConfiguration(new String(config, Charsets.UTF_8));
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    public void configure(String config) throws TaskHandlerException {
        throw new UnsupportedOperationException();
    }

    /**
     * Task handler implementation
     */
    @Override
    public Map<String, Object> handle(final Subject subject, final IVariableProvider variableProvider, final WfTask task) throws Exception {
        return new AlfSessionWrapper<Map<String, Object>>() {
            @Override
            protected Map<String, Object> code() throws Exception {
                HandlerData handlerData = new HandlerData(inputParams, outputParams, subject, variableProvider, task);
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
                return handlerData.getOutputVariables();
            }
        }.runInSession();
    }

    public static class ParamDef {
        public final String name;
        public final String variableName;
        public final String value;

        public ParamDef(Element element) {
            name = element.attributeValue("name");
            variableName = element.attributeValue("variable");
            value = element.attributeValue("value");
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

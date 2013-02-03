package ru.runa.wf.logic.bot;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.handler.ParamDef;
import ru.runa.wfe.handler.ParamsDef;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class BotTaskConfigurationUtils {
    private static final Log log = LogFactory.getLog(BotTaskConfigurationUtils.class);
    protected static final String INPUT_PARAM = "input";
    protected static final String OUTPUT_PARAM = "output";
    protected static final String PARAMETER_PARAM = "param";
    protected static final String TASK_PARAM = "task";
    protected static final String NAME_PARAM = "name";
    protected static final String VALUE_PARAM = "value";
    protected static final String VARIABLE_PARAM = "variable";
    protected static final String PARAMETERS_PARAM = "parameters";
    protected static final String BOTCONFIG_PARAM = "botconfig";

    public static boolean isExtendedBotTaskConfiguration(byte[] configuration) {
        try {
            if (configuration != null) {
                Document document = XmlUtils.parseWithoutValidation(configuration);
                Element paramConfigElement = document.getRootElement().element(PARAMETERS_PARAM);
                Element botConfigElement = document.getRootElement().element(BOTCONFIG_PARAM);
                return paramConfigElement != null && botConfigElement != null;
            }
        } catch (Exception e) {
            log.debug("Unable to determine is bot task extended or not from configuration", e);
        }
        return false;
    }

    public static String getBotTaskName(User user, WfTask task) throws AuthorizationException {
        Element taskElement = getBotTaskElement(user, task);
        if (taskElement != null) {
            return taskElement.attributeValue("botTaskName");
        }
        return task.getName();
    }

    private static Element getBotTaskElement(User user, WfTask task) throws AuthorizationException {
        byte[] xml = Delegates.getDefinitionService().getFile(user, task.getDefinitionId(), IFileDataProvider.BOTS_XML_FILE);
        if (xml == null) {
            // this is the case of simple bot task
            return null;
        }
        Document document = XmlUtils.parseWithoutValidation(xml);
        List<Element> elements = document.getRootElement().elements("task");
        for (Element element : elements) {
            if (Objects.equal(task.getName(), element.attributeValue("name"))) {
                return element;
            }
        }
        return null;
    }

    public static byte[] substituteConfiguration(User user, WfTask task, byte[] extendedConfiguration, IVariableProvider variableProvider) {
        if (extendedConfiguration == null) {
            return null;
        }
        Document document = XmlUtils.parseWithoutValidation(extendedConfiguration);
        Element taskElement = getBotTaskElement(user, task);
        Element configElement = taskElement.element("config");
        if (configElement == null) {
            return extendedConfiguration;
        }
        Element paramConfigElement = document.getRootElement().element(PARAMETERS_PARAM);
        ParamsDef botTaskParamsDef = ParamsDef.parse(paramConfigElement);
        ParamsDef taskParamsDef = ParamsDef.parse(configElement);
        Element configurationElement = document.getRootElement().element(BOTCONFIG_PARAM);
        String substituted = XmlUtils.toString(configurationElement);
        for (ParamDef botTaskParamDef : botTaskParamsDef.getInputParams().values()) {
            ParamDef taskParamDef = taskParamsDef.getInputParamNotNull(botTaskParamDef.getName());
            String replacement = getReplacement(taskParamDef);
            substituted = substituted.replaceAll("\"" + taskParamDef.getName() + "\"", "\"" + replacement + "\"");
        }
        return substituted.getBytes(Charsets.UTF_8);
    }

    private static String getReplacement(ParamDef paramDef) {
        if (!Strings.isNullOrEmpty(paramDef.getVariableName())) {
            return paramDef.getVariableName();
        } else if (!Strings.isNullOrEmpty(paramDef.getVariableName())) {
            return paramDef.getVariableName();
        } else {
            throw new InternalApplicationException("no replacement found for param " + paramDef);
        }
    }

}

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
import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class BotTaskConfigurationUtils {
    private static final Log log = LogFactory.getLog(BotTaskConfigurationUtils.class);
    private static final String TASK_PARAM = "task";
    private static final String BOT_TASK_NAME_PARAM = "botTaskName";
    private static final String NAME_PARAM = "name";
    private static final String PARAMETERS_PARAM = "parameters";
    private static final String BOTCONFIG_PARAM = "botconfig";
    private static final String CONFIG_PARAM = "config";

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

    public static String getBotTaskName(User user, WfTask task) {
        Element taskElement = getBotTaskElement(user, task);
        if (taskElement != null) {
            return taskElement.attributeValue(BOT_TASK_NAME_PARAM);
        }
        return task.getName();
    }

    private static Element getBotTaskElement(User user, WfTask task) {
        byte[] xml = Delegates.getDefinitionService().getFile(user, task.getDefinitionId(), IFileDataProvider.BOTS_XML_FILE);
        if (xml == null) {
            // this is the case of simple bot task
            return null;
        }
        Document document = XmlUtils.parseWithoutValidation(xml);
        List<Element> elements = document.getRootElement().elements(TASK_PARAM);
        for (Element element : elements) {
            if (Objects.equal(task.getName(), element.attributeValue(NAME_PARAM))) {
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
        Element configElement = taskElement.element(CONFIG_PARAM);
        if (configElement == null) {
            return extendedConfiguration;
        }
        Element parametersElement = document.getRootElement().element(PARAMETERS_PARAM);
        ParamsDef botTaskParamsDef = ParamsDef.parse(parametersElement.element(CONFIG_PARAM));
        ParamsDef taskParamsDef = ParamsDef.parse(configElement);
        String substituted = document.getRootElement().element(BOTCONFIG_PARAM).getTextTrim();
        for (ParamDef botTaskParamDef : botTaskParamsDef.getInputParams().values()) {
            ParamDef taskParamDef = taskParamsDef.getInputParamNotNull(botTaskParamDef.getName());
            String replacement = getReplacement(taskParamDef);
            substituted = substituted.replaceAll("\"" + taskParamDef.getName() + "\"", "\"" + replacement + "\"");
        }
        for (ParamDef botTaskParamDef : botTaskParamsDef.getOutputParams().values()) {
            ParamDef taskParamDef = taskParamsDef.getOutputParamNotNull(botTaskParamDef.getName());
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

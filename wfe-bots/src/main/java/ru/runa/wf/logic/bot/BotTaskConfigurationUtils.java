package ru.runa.wf.logic.bot;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class BotTaskConfigurationUtils {
    private static final Log log = LogFactory.getLog(BotTaskConfigurationUtils.class);
    private static final String TASK_PARAM = "task";
    private static final String BOT_TASK_NAME_PARAM = "botTaskName";
    private static final String ID_PARAM = "id";
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
            log.debug("Unable to determine is bot task extended or not from configuration: " + e);
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
        byte[] xml = Delegates.getDefinitionService().getProcessDefinitionFile(user, task.getDefinitionId(), IFileDataProvider.BOTS_XML_FILE);
        if (xml == null) {
            // this is the case of simple bot task
            return null;
        }
        Document document = XmlUtils.parseWithoutValidation(xml);
        List<Element> elements = document.getRootElement().elements(TASK_PARAM);
        for (Element element : elements) {
            if (Objects.equal(task.getNodeId(), element.attributeValue(ID_PARAM))) {
                return element;
            }
        }
        return null;
    }

    public static byte[] substituteExtendedConfiguration(User user, WfTask task, byte[] configuration, IVariableProvider variableProvider) {
        if (configuration == null) {
            return null;
        }
        Document document = XmlUtils.parseWithoutValidation(configuration);
        Element taskElement = getBotTaskElement(user, task);
        Preconditions.checkNotNull(taskElement, "Unable to get bot task link xml");
        Element configElement = taskElement.element(CONFIG_PARAM);
        if (configElement == null) {
            return configuration;
        }
        Element parametersElement = document.getRootElement().element(PARAMETERS_PARAM);
        ParamsDef botTaskParamsDef = ParamsDef.parse(parametersElement.element(CONFIG_PARAM));
        ParamsDef taskParamsDef = ParamsDef.parse(configElement);
        Element botConfigElement = document.getRootElement().element(BOTCONFIG_PARAM);
        String substituted;
        if (botConfigElement.elements().size() > 0) {
            Element taskConfigElement = (Element) botConfigElement.elements().get(0);
            substituted = XmlUtils.toString(taskConfigElement, OutputFormat.createPrettyPrint());
        } else {
            substituted = botConfigElement.getText();
        }
        for (ParamDef botTaskParamDef : botTaskParamsDef.getInputParams().values()) {
            ParamDef taskParamDef = taskParamsDef.getInputParamNotNull(botTaskParamDef.getName());
            substituted = substituteParameter(substituted, botTaskParamDef, taskParamDef);
        }
        for (ParamDef botTaskParamDef : botTaskParamsDef.getOutputParams().values()) {
            ParamDef taskParamDef = taskParamsDef.getOutputParamNotNull(botTaskParamDef.getName());
            substituted = substituteParameter(substituted, botTaskParamDef, taskParamDef);
        }
        return substituted.getBytes(Charsets.UTF_8);
    }

    private static String substituteParameter(String config, ParamDef botTaskParamDef, ParamDef taskParamDef) {
        String replacement;
        if (!Strings.isNullOrEmpty(taskParamDef.getVariableName())) {
            replacement = taskParamDef.getVariableName();
        } else if (!Strings.isNullOrEmpty(taskParamDef.getValue())) {
            replacement = taskParamDef.getValue();
        } else {
            throw new InternalApplicationException("no replacement found for param " + taskParamDef);
        }
        config = config.replaceAll(Pattern.quote("param:" + taskParamDef.getName()), Matcher.quoteReplacement(replacement));
        return config;
    }

    public static boolean isParameterizedBotTaskConfiguration(byte[] configuration) {
        try {
            if (configuration != null) {
                Document document = XmlUtils.parseWithoutValidation(configuration);
                ParamsDef paramsDef = ParamsDef.parse(document.getRootElement());
                return paramsDef.getInputParams().size() + paramsDef.getOutputParams().size() > 0;
            }
        } catch (Exception e) {
            log.debug("Unable to determine is bot task parameterized or not from configuration: " + e);
        }
        return false;
    }

    public static byte[] substituteParameterizedConfiguration(User user, WfTask task, byte[] configuration, IVariableProvider variableProvider) {
        Element taskElement = getBotTaskElement(user, task);
        if (taskElement == null) {
            return configuration;
        }
        Element configElement = taskElement.element(CONFIG_PARAM);
        ParamsDef taskParamsDef = ParamsDef.parse(configElement);

        Document document = XmlUtils.parseWithoutValidation(configuration);
        Element root = document.getRootElement();
        Element inputElement = root.element("input");
        if (inputElement != null) {
            List<Element> inputParamElements = inputElement.elements("param");
            for (Element element : inputParamElements) {
                String paramName = element.attributeValue("name");
                ParamDef paramDef = taskParamsDef.getInputParam(paramName);
                if (paramDef == null) {
                    // optional parameter
                    continue;
                }
                if (!Strings.isNullOrEmpty(paramDef.getVariableName())) {
                    element.addAttribute("variable", paramDef.getVariableName());
                } else if (!Strings.isNullOrEmpty(paramDef.getValue())) {
                    element.addAttribute("value", paramDef.getValue());
                }
            }
        }
        Element outputElement = root.element("output");
        if (outputElement != null) {
            List<Element> outputParamElements = outputElement.elements("param");
            for (Element element : outputParamElements) {
                String paramName = element.attributeValue("name");
                ParamDef paramDef = taskParamsDef.getOutputParam(paramName);
                if (paramDef == null) {
                    // optional parameter
                    continue;
                }
                if (!Strings.isNullOrEmpty(paramDef.getVariableName())) {
                    element.addAttribute("variable", paramDef.getVariableName());
                } else if (!Strings.isNullOrEmpty(paramDef.getValue())) {
                    element.addAttribute("value", paramDef.getValue());
                }
            }
        }
        return XmlUtils.save(document);
    }
}

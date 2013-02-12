package ru.runa.gpd.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.editor.BotTaskConfigHelper;
import ru.runa.gpd.lang.model.BotTask;

import com.google.common.base.Strings;

public class BotXmlUtil extends XmlUtil {
    private final static String NAME_ATTRIBUTE_NAME = "name";
    private final static String PASSWORD_ATTRIBUTE_NAME = "password";
    private final static String STARTTIMEOUT_ATTRIBUTE_NAME = "startTimeout";
    private final static String HANDLER_ATTRIBUTE_NAME = "handler";
    private final static String CONFIGURATION_STRING_ATTRIBUTE_NAME = "configuration";
    private final static String ADD_BOT_CONFIGURATION_ELEMENT_NAME = "addConfigurationsToBot";
    private final static String BOT_CONFIGURATION_ELEMENT_NAME = "botConfiguration";

    public static Document createScriptForBotLoading(String botName, List<BotTask> tasks) {
        Document script = XmlUtil.createDocument("workflowScript");
        Element rootElement = script.getRootElement();
        rootElement.addAttribute("xmlns", "http://runa.ru/xml");
        rootElement.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.addAttribute("xsi:schemaLocation", "http://runa.ru/xml workflowScript.xsd");
        Element createBotElement = rootElement.addElement("createBot");
        createBotElement.addAttribute(NAME_ATTRIBUTE_NAME, botName);
        createBotElement.addAttribute(PASSWORD_ATTRIBUTE_NAME, "");
        createBotElement.addAttribute(STARTTIMEOUT_ATTRIBUTE_NAME, "");
        if (tasks.size() > 0) {
            Element removeTasks = rootElement.addElement("removeConfigurationsFromBot");
            removeTasks.addAttribute(NAME_ATTRIBUTE_NAME, botName);
            for (BotTask task : tasks) {
                Element taskElement = removeTasks.addElement("botConfiguration");
                taskElement.addAttribute(NAME_ATTRIBUTE_NAME, task.getName());
            }
            Element addTasks = rootElement.addElement("addConfigurationsToBot");
            addTasks.addAttribute(NAME_ATTRIBUTE_NAME, botName);
            for (BotTask task : tasks) {
                Element taskElement = addTasks.addElement("botConfiguration");
                taskElement.addAttribute(NAME_ATTRIBUTE_NAME, task.getName());
                taskElement.addAttribute(HANDLER_ATTRIBUTE_NAME, task.getClazz());
                if (task.getConfig() != null) {
                    taskElement.addAttribute(CONFIGURATION_STRING_ATTRIBUTE_NAME, task.getName() + ".conf");
                }
            }
        }
        return script;
    }

    public static List<BotTask> getBotTasksFromScript(InputStream inputStream) {
        List<BotTask> botTasks = new ArrayList<BotTask>();
        Document document = XmlUtil.parseWithoutValidation(inputStream);
        List<Element> taskNodeList = document.getRootElement().elements(ADD_BOT_CONFIGURATION_ELEMENT_NAME);
        for (Element taskElement : taskNodeList) {
            List<Element> botList = taskElement.elements(BOT_CONFIGURATION_ELEMENT_NAME);
            for (Element botElement : botList) {
                String name = botElement.attributeValue(NAME_ATTRIBUTE_NAME);
                if (Strings.isNullOrEmpty(name)) {
                    continue;
                }
                String handler = botElement.attributeValue(HANDLER_ATTRIBUTE_NAME, "");
                BotTask task = new BotTask();
                task.setName(name);
                task.setClazz(handler);
                task.setDelegationClassName(handler);
                String fileConfig = botElement.attributeValue(CONFIGURATION_STRING_ATTRIBUTE_NAME);
                task.setConfig(fileConfig);
                task.setDelegationConfiguration(fileConfig);
                task.setParamDefConfig(BotTaskConfigHelper.createEmptyParamDefConfig());
                botTasks.add(task);
            }
        }
        return botTasks;
    }
}

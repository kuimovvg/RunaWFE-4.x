package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.lang.model.BotTaskLink;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Strings;

public class BotsXmlContentProvider extends AuxContentProvider {
    private static final String BOTS_XML_FILE_NAME = "bots.xml";
    private static final String TASK_ELEMENT_NAME = "task";
    private static final String BOTTASKS_ELEMENT_NAME = "bottasks";
    private static final String CLASS_ATTRIBUTE_NAME = "class";
    private static final String BOT_TASK_NAME = "botTaskName";
    private static final String CONFIG_ELEMENT_NAME = "config";

    @Override
    public void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception {
        IFile file = folder.getFile(BOTS_XML_FILE_NAME);
        if (!file.exists()) {
            return;
        }
        Document document = XmlUtil.parseWithoutValidation(file.getContents());
        List<Element> elements = document.getRootElement().elements(TASK_ELEMENT_NAME);
        for (Element element : elements) {
            String taskId = element.attributeValue(ID_ATTRIBUTE_NAME, element.attributeValue(NAME_ATTRIBUTE_NAME));
            String className = element.attributeValue(CLASS_ATTRIBUTE_NAME);
            String botTaskName = element.attributeValue(BOT_TASK_NAME);
            Element configElement = element.element(CONFIG_ELEMENT_NAME);
            String configuration = configElement != null ? XmlUtil.toString(configElement) : null;
            TaskState taskState = definition.getGraphElementByIdNotNull(taskId);
            BotTaskLink botTaskLink = new BotTaskLink();
            botTaskLink.setBotTaskName(botTaskName);
            botTaskLink.setDelegationClassName(className);
            botTaskLink.setDelegationConfiguration(configuration);
            taskState.setBotTaskLink(botTaskLink);
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(BOTTASKS_ELEMENT_NAME);
        int botTasksCount = 0;
        for (TaskState taskState : definition.getChildren(TaskState.class)) {
            BotTaskLink botTaskLink = taskState.getBotTaskLink();
            if (botTaskLink != null) {
                botTasksCount++;
                Element element = document.getRootElement().addElement(TASK_ELEMENT_NAME);
                element.addAttribute(ID_ATTRIBUTE_NAME, taskState.getId());
                element.addAttribute(CLASS_ATTRIBUTE_NAME, botTaskLink.getDelegationClassName());
                element.addAttribute(BOT_TASK_NAME, botTaskLink.getBotTaskName());
                if (!Strings.isNullOrEmpty(botTaskLink.getDelegationConfiguration())) {
                    Document confDocument = XmlUtil.parseWithoutValidation(botTaskLink.getDelegationConfiguration());
                    element.add(confDocument.getRootElement().detach());
                }
            }
        }
        if (botTasksCount == 0) {
            if (folder.getFile(BOTS_XML_FILE_NAME).exists()) {
                folder.getFile(BOTS_XML_FILE_NAME).delete(true, null);
            }
        } else {
            byte[] bytes = XmlUtil.writeXml(document, OutputFormat.createPrettyPrint());
            updateFile(folder.getFile(BOTS_XML_FILE_NAME), bytes);
        }
    }
}

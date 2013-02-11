package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionsRegistry;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.util.BotTaskContentUtil;
import ru.runa.gpd.util.ProjectFinder;
import ru.runa.gpd.util.XmlUtil;

public class BotsXmlContentProvider extends AuxContentProvider {
    private static final String BOTS_XML_FILE_NAME = "bots.xml";
    private static final String TASK_ELEMENT_NAME = "task";
    private static final String BOTTASKS_ELEMENT_NAME = "bottasks";
    private static final String CLASS_ATTRIBUTE_NAME = "class";
    private static final String BOT_TASK_NAME = "botTaskName";
    private static final String CONF_PARAM = "config";
    private static final String INPUT_PARAM = "input";
    private static final String OUTPUT_PARAM = "output";

    @Override
    public void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception {
        IFile file = folder.getFile(BOTS_XML_FILE_NAME);
        if (!file.exists()) {
            return;
        }
        Document document = XmlUtil.parseWithoutValidation(file.getContents());
        List<Element> elements = document.getRootElement().elements(TASK_ELEMENT_NAME);
        for (Element element : elements) {
            String taskName = element.attributeValue(NAME_ATTRIBUTE_NAME);
            String className = element.attributeValue(CLASS_ATTRIBUTE_NAME);
            String botTaskName = element.attributeValue(BOT_TASK_NAME);
            Document confDocument = XmlUtil.createDocument(CONF_PARAM);
            Element inputParamElement = element.element(INPUT_PARAM);
            Element outputParamElement = element.element(OUTPUT_PARAM);
            if (inputParamElement != null || outputParamElement != null) {
                if (inputParamElement != null) {
                    confDocument.add(inputParamElement.detach());
                }
                if (outputParamElement != null) {
                    confDocument.add(outputParamElement.detach());
                }
            } else {
                confDocument = null;
            }
            for (TaskState taskState : definition.getChildren(TaskState.class)) {
                if (taskState.getName().equals(taskName)) {
                    BotTask botTask = new BotTask();
                    botTask.setName(botTaskName);
                    botTask.setClazz(className);
                    if (confDocument != null) {
                        String conf = XmlUtil.toString(confDocument);
                        botTask.setDelegationConfiguration(conf);
                    }
                    botTask.setDelegationClassName(className);
                    botTask.setProcessDefinition(definition);
                    Swimlane selectedSwimlane = taskState.getSwimlane();
                    if (selectedSwimlane != null && selectedSwimlane.getDelegationConfiguration() != null) {
                        OrgFunctionDefinition orgFunctionDefinition = OrgFunctionsRegistry.parseSwimlaneConfiguration(selectedSwimlane.getDelegationConfiguration());
                        if (orgFunctionDefinition != null && BotTask.BOT_EXECUTOR_SWIMLANE_NAME.equals(orgFunctionDefinition.getName())) {
                            if (orgFunctionDefinition.getParameters().size() > 0) {
                                String value = orgFunctionDefinition.getParameters().get(0).getValue();
                                for (IFolder botFolder : ProjectFinder.getAllBotFolders()) {
                                    if (botFolder.getName().equals(value)) {
                                        for (IFile botTaskFile : ProjectFinder.getBotTaskFiles(botFolder)) {
                                            if (botTaskFile.getName().equals(botTaskName)) {
                                                BotTask botTaskFromFile = BotTaskContentUtil.getBotTaskFromFile(botTaskFile);
                                                botTask.setConfig(botTaskFromFile.getConfig());
                                                botTask.setParamDefConfig(botTaskFromFile.getParamDefConfig());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    taskState.setBotTask(botTask);
                }
            }
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(BOTTASKS_ELEMENT_NAME);
        int botTaskCount = 0;
        for (TaskState taskState : definition.getChildren(TaskState.class)) {
            BotTask botTask = taskState.getBotTask();
            if (botTask != null) {
                botTaskCount++;
                Element element = document.addElement(TASK_ELEMENT_NAME);
                element.addAttribute(NAME_ATTRIBUTE_NAME, taskState.getName());
                element.addAttribute(CLASS_ATTRIBUTE_NAME, botTask.getDelegationClassName());
                element.addAttribute(BOT_TASK_NAME, botTask.getName());
                if (botTask.getDelegationConfiguration() != null && botTask.getDelegationConfiguration().length() > 0) {
                    Document confDocument = XmlUtil.parseWithoutValidation(botTask.getDelegationConfiguration());
                    element.add(confDocument.getRootElement().detach());
                }
            }
        }
        if (botTaskCount == 0) {
            if (folder.getFile(BOTS_XML_FILE_NAME).exists()) {
                folder.getFile(BOTS_XML_FILE_NAME).delete(true, null);
            }
        } else {
            byte[] bytes = XmlUtil.writeXml(document);
            updateFile(folder.getFile(BOTS_XML_FILE_NAME), bytes);
        }
    }
}

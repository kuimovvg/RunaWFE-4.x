package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.util.XmlUtil;

public class SubstitutionExceptionsXmlContentProvider extends AuxContentProvider {
    private static final String XML_FILE_NAME = "substitutionExceptions.xml";
    private static final String TASK_ELEMENT_NAME = "task";
    private static final String EXCEPTIONS_ELEMENT_NAME = "ignoreSubstitutions";

    @Override
    public void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception {
        IFile file = folder.getFile(XML_FILE_NAME);
        if (!file.exists()) {
            return;
        }
        Document document = XmlUtil.parseWithoutValidation(file.getContents());
        List<Element> elementsList = document.getRootElement().elements(TASK_ELEMENT_NAME);
        for (Element element : elementsList) {
            String taskName = element.attributeValue(NAME_ATTRIBUTE_NAME);
            try {
                TaskState taskState = (TaskState) definition.getNodeByName(taskName);
                taskState.setIgnoreSubstitution(true);
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("No swimlane found for " + taskName, e);
            }
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(EXCEPTIONS_ELEMENT_NAME);
        Element root = document.getRootElement();
        boolean atLeastOneExceptionExists = false;
        for (TaskState taskState : definition.getChildren(TaskState.class)) {
            if (taskState.isIgnoreSubstitution()) {
                atLeastOneExceptionExists = true;
                Element element = root.addElement(TASK_ELEMENT_NAME);
                element.addAttribute(NAME_ATTRIBUTE_NAME, taskState.getName());
            }
        }
        if (atLeastOneExceptionExists) {
            byte[] bytes = XmlUtil.writeXml(document);
            updateFile(folder.getFile(XML_FILE_NAME), bytes);
        }
    }
}

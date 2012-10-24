package org.jbpm.ui.par;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.jpdl3.model.TaskState;
import org.jbpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
        Document document = XmlUtil.parseDocument(file.getContents());
        NodeList elementsList = document.getDocumentElement().getElementsByTagName(TASK_ELEMENT_NAME);
        for (int j = 0; j < elementsList.getLength(); j++) {
            Element element = (Element) elementsList.item(j);
            String taskName = element.getAttribute(NAME_ATTRIBUTE_NAME);
            try {
                TaskState taskState = (TaskState) definition.getNodeByName(taskName);
                taskState.setIgnoreSubstitution(true);
            } catch (Exception e) {
                DesignerLogger.logErrorWithoutDialog("No swimlane found for " + taskName, e);
            }
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(EXCEPTIONS_ELEMENT_NAME, null);
        Element root = document.getDocumentElement();
        boolean atLeastOneExceptionExists = false;
        for (TaskState taskState : definition.getChildren(TaskState.class)) {
            if (taskState.isIgnoreSubstitution()) {
                atLeastOneExceptionExists = true;
                Element element = document.createElement(TASK_ELEMENT_NAME);
                element.setAttribute(NAME_ATTRIBUTE_NAME, taskState.getName());
                root.appendChild(element);
            }
        }
        if (atLeastOneExceptionExists) {
            byte[] bytes = XmlUtil.writeXml(document);
            updateFile(folder.getFile(XML_FILE_NAME), bytes);
        }
    }

}

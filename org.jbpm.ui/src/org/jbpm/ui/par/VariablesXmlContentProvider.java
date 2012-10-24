package org.jbpm.ui.par;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Swimlane;
import org.jbpm.ui.common.model.Variable;
import org.jbpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class VariablesXmlContentProvider extends AuxContentProvider {

    private static final String VARIABLES_XML_FILE_NAME = "variables.xml";
    private static final String FORMAT_ATTRIBUTE_NAME = "format";
    private static final String SWIMLANE_ATTRIBUTE_NAME = "swimlane";
    private static final String DESCRIPTION_ATTRIBUTE_NAME = "description";
    private static final String VARIABLE_ELEMENT_NAME = "variable";
    private static final String VARIABLES_ELEMENT_NAME = "variables";
    private static final String PUBLUC_ATTRIBUTE_NAME = "public";
    private static final String DEFAULT_VALUE_ATTRIBUTE_NAME = "defaultValue";

    @Override
    public void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception {
        IFile file = folder.getFile(VARIABLES_XML_FILE_NAME);
        if (!file.exists()) {
            return;
        }
        Document document = XmlUtil.parseDocument(file.getContents());
        NodeList elementsList = document.getDocumentElement().getElementsByTagName(VARIABLE_ELEMENT_NAME);
        for (int j = 0; j < elementsList.getLength(); j++) {
            Element element = (Element) elementsList.item(j);
            String variableName = element.getAttribute(NAME_ATTRIBUTE_NAME);
            String formatName = element.getAttribute(FORMAT_ATTRIBUTE_NAME);
            if (isEmptyOrNull(formatName)) {
                formatName = "org.jbpm.web.formgen.format.DefaultFormat";
            }
            String description = element.getAttribute(DESCRIPTION_ATTRIBUTE_NAME);
            String swimlaneName = element.getAttribute(SWIMLANE_ATTRIBUTE_NAME);
            String publicVisibilityStr = element.getAttribute(PUBLUC_ATTRIBUTE_NAME);
            boolean publicVisibility = "true".equals(publicVisibilityStr);
            String defaultValue = element.getAttribute(DEFAULT_VALUE_ATTRIBUTE_NAME);
            if (swimlaneName != null && Boolean.parseBoolean(swimlaneName)) {
                try {
                    Swimlane swimlane = definition.getSwimlaneByName(variableName);
                    swimlane.setDescription(description);
                } catch (Exception e) {
                    DesignerLogger.logErrorWithoutDialog("No swimlane found for " + variableName, e);
                }
                continue;
            }
            Variable variable = new Variable(variableName, formatName, publicVisibility, defaultValue);
            variable.setDescription(description);
            definition.addVariable(variable);
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(VARIABLES_ELEMENT_NAME, null);
        Element root = document.getDocumentElement();
        for (Variable variable : definition.getVariablesList()) {
            Element element = document.createElement(VARIABLE_ELEMENT_NAME);
            root.appendChild(element);
            element.setAttribute(NAME_ATTRIBUTE_NAME, variable.getName());
            element.setAttribute(FORMAT_ATTRIBUTE_NAME, variable.getFormat());
            if (variable.isPublicVisibility()) {
                element.setAttribute(PUBLUC_ATTRIBUTE_NAME, "true");
            }
            if (variable.getDescription() != null) {
                element.setAttribute(DESCRIPTION_ATTRIBUTE_NAME, variable.getDescription());
            }
            if (variable.getDefaultValue() != null && variable.getDefaultValue().length() > 0) {
                element.setAttribute(DEFAULT_VALUE_ATTRIBUTE_NAME, variable.getDefaultValue());
            }
        }
        for (Swimlane swimlane : definition.getSwimlanes()) {
            Element element = document.createElement(VARIABLE_ELEMENT_NAME);
            root.appendChild(element);
            element.setAttribute(NAME_ATTRIBUTE_NAME, swimlane.getName());
            element.setAttribute(FORMAT_ATTRIBUTE_NAME, "ru.runa.wf.web.forms.format.StringFormat");
            element.setAttribute(SWIMLANE_ATTRIBUTE_NAME, "true");
            if (swimlane.isPublicVisibility()) {
                element.setAttribute(PUBLUC_ATTRIBUTE_NAME, "true");
            }
            if (swimlane.getDescription() != null) {
                element.setAttribute(DESCRIPTION_ATTRIBUTE_NAME, swimlane.getDescription());
            }
        }
        byte[] bytes = XmlUtil.writeXml(document);
        updateFile(folder.getFile(VARIABLES_XML_FILE_NAME), bytes);
    }

}

package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.var.format.ExecutorFormat;

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
        Document document = XmlUtil.parseWithoutValidation(file.getContents());
        List<Element> elementsList = document.getRootElement().elements(VARIABLE_ELEMENT_NAME);
        for (Element element : elementsList) {
            String variableName = element.attributeValue(NAME_ATTRIBUTE_NAME);
            String formatName = element.attributeValue(FORMAT_ATTRIBUTE_NAME);
            formatName = BackCompatibilityClassNames.getClassName(formatName);
            String description = element.attributeValue(DESCRIPTION_ATTRIBUTE_NAME);
            String swimlaneName = element.attributeValue(SWIMLANE_ATTRIBUTE_NAME);
            String publicVisibilityStr = element.attributeValue(PUBLUC_ATTRIBUTE_NAME);
            boolean publicVisibility = "true".equals(publicVisibilityStr);
            String defaultValue = element.attributeValue(DEFAULT_VALUE_ATTRIBUTE_NAME);
            if (swimlaneName != null && Boolean.parseBoolean(swimlaneName)) {
                try {
                    Swimlane swimlane = definition.getSwimlaneByName(variableName);
                    swimlane.setDescription(description);
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("No swimlane found for " + variableName, e);
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
        Document document = XmlUtil.createDocument(VARIABLES_ELEMENT_NAME);
        Element root = document.getRootElement();
        for (Variable variable : definition.getVariables()) {
            Element element = root.addElement(VARIABLE_ELEMENT_NAME);
            element.addAttribute(NAME_ATTRIBUTE_NAME, variable.getName());
            element.addAttribute(FORMAT_ATTRIBUTE_NAME, variable.getFormat());
            if (variable.isPublicVisibility()) {
                element.addAttribute(PUBLUC_ATTRIBUTE_NAME, "true");
            }
            if (variable.getDescription() != null) {
                element.addAttribute(DESCRIPTION_ATTRIBUTE_NAME, variable.getDescription());
            }
            if (variable.getDefaultValue() != null && variable.getDefaultValue().length() > 0) {
                element.addAttribute(DEFAULT_VALUE_ATTRIBUTE_NAME, variable.getDefaultValue());
            }
        }
        for (Swimlane swimlane : definition.getSwimlanes()) {
            Element element = root.addElement(VARIABLE_ELEMENT_NAME);
            element.addAttribute(NAME_ATTRIBUTE_NAME, swimlane.getName());
            element.addAttribute(FORMAT_ATTRIBUTE_NAME, ExecutorFormat.class.getName());
            element.addAttribute(SWIMLANE_ATTRIBUTE_NAME, "true");
            if (swimlane.isPublicVisibility()) {
                element.addAttribute(PUBLUC_ATTRIBUTE_NAME, "true");
            }
            if (swimlane.getDescription() != null) {
                element.addAttribute(DESCRIPTION_ATTRIBUTE_NAME, swimlane.getDescription());
            }
        }
        byte[] bytes = XmlUtil.writeXml(document);
        updateFile(folder.getFile(VARIABLES_XML_FILE_NAME), bytes);
    }
}

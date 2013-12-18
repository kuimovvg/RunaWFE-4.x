package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.commons.BackCompatibilityClassNames;

public class VariablesXmlContentProvider extends AuxContentProvider {
    private static final String XML_FILE_NAME = "variables.xml";
    private static final String FORMAT_ATTRIBUTE_NAME = "format";
    private static final String SWIMLANE_ATTRIBUTE_NAME = "swimlane";
    private static final String DESCRIPTION_ATTRIBUTE_NAME = "description";
    private static final String VARIABLE_ELEMENT_NAME = "variable";
    private static final String VARIABLES_ELEMENT_NAME = "variables";
    private static final String PUBLUC_ATTRIBUTE_NAME = "public";
    private static final String DEFAULT_VALUE_ATTRIBUTE_NAME = "defaultValue";
    private static final String SCRIPTING_NAME_ATTRIBUTE_NAME = "scriptingName";

    @Override
    public boolean isSupportedForEmbeddedSubprocess() {
        return false;
    }
    
    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }
    
    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> elementsList = document.getRootElement().elements(VARIABLE_ELEMENT_NAME);
        for (Element element : elementsList) {
            String variableName = element.attributeValue(NAME_ATTRIBUTE_NAME);
            String format = element.attributeValue(FORMAT_ATTRIBUTE_NAME);
            format = BackCompatibilityClassNames.getClassName(format);
            String description = element.attributeValue(DESCRIPTION_ATTRIBUTE_NAME);
            String isSwimlane = element.attributeValue(SWIMLANE_ATTRIBUTE_NAME);
            String publicVisibilityStr = element.attributeValue(PUBLUC_ATTRIBUTE_NAME);
            boolean publicVisibility = "true".equals(publicVisibilityStr);
            String defaultValue = element.attributeValue(DEFAULT_VALUE_ATTRIBUTE_NAME);
            String scriptingName = element.attributeValue(SCRIPTING_NAME_ATTRIBUTE_NAME, variableName);
            if ("true".equals(isSwimlane)) {
                try {
                    Swimlane swimlane = definition.getSwimlaneByName(variableName);
                    swimlane.setScriptingName(scriptingName);
                    swimlane.setDescription(description);
                    swimlane.setPublicVisibility(publicVisibility);
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("No swimlane found for " + variableName, e);
                }
                continue;
            }
            Variable variable = new Variable(variableName, scriptingName, format, publicVisibility, defaultValue);
            variable.setDescription(description);
            definition.addVariable(variable);
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(VARIABLES_ELEMENT_NAME);
        Element root = document.getRootElement();
        for (Variable variable : definition.getVariables(true)) {
            Element element = root.addElement(VARIABLE_ELEMENT_NAME);
            element.addAttribute(NAME_ATTRIBUTE_NAME, variable.getName());
            element.addAttribute(SCRIPTING_NAME_ATTRIBUTE_NAME, variable.getScriptingName());
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
            element.addAttribute(SWIMLANE_ATTRIBUTE_NAME, String.valueOf(variable instanceof Swimlane));
        }
        return document;
    }
}

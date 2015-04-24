package ru.runa.wfe.definition.par;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableUserType;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class VariableDefinitionParser implements ProcessArchiveParser {
    private static final String FORMAT = "format";
    private static final String SWIMLANE = "swimlane";
    private static final String NAME = "name";
    private static final String VARIABLE = "variable";
    private static final String PUBLIC = "public";
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final String SCRIPTING_NAME = "scriptingName";
    private static final String USER_TYPE = "usertype";
    private static final String DESCRIPTION = "description";

    @Autowired
    private LocalizationDAO localizationDAO;

    public void setLocalizationDAO(LocalizationDAO localizationDAO) {
        this.localizationDAO = localizationDAO;
    }

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return false;
    }

    @Override
    public void readFromArchive(ProcessArchive archive, ProcessDefinition processDefinition) {
        byte[] xml = processDefinition.getFileDataNotNull(IFileDataProvider.VARIABLES_XML_FILE_NAME);
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        Map<String, VariableUserType> userTypes = Maps.newHashMap();
        List<Element> typeElements = document.getRootElement().elements(USER_TYPE);
        for (Element typeElement : typeElements) {
            VariableUserType type = new VariableUserType(typeElement.attributeValue(NAME));
            userTypes.put(type.getName(), type);
        }
        for (Element typeElement : typeElements) {
            VariableUserType type = userTypes.get(typeElement.attributeValue(NAME));
            List<Element> attributeElements = typeElement.elements(VARIABLE);
            for (Element element : attributeElements) {
                VariableDefinition variable = parse(processDefinition, element, userTypes);
                type.getAttributes().add(variable);
            }
        }
        List<Element> variableElements = root.elements(VARIABLE);
        for (Element element : variableElements) {
            boolean swimlane = Boolean.parseBoolean(element.attributeValue(SWIMLANE, "false"));
            if (swimlane) {
                String name = element.attributeValue(NAME);
                String scriptingName = element.attributeValue(SCRIPTING_NAME, name);
                processDefinition.getSwimlane(name).setScriptingName(scriptingName);
            } else {
                VariableDefinition variable = parse(processDefinition, element, userTypes);
                processDefinition.addVariable(variable.getName(), variable);
            }
        }
    }

    private VariableDefinition parse(ProcessDefinition processDefinition, Element element, Map<String, VariableUserType> userTypes) {
        String name = element.attributeValue(NAME);
        String scriptingName = element.attributeValue(SCRIPTING_NAME, name);
        VariableDefinition variableDefinition = new VariableDefinition(false, name, scriptingName);
        variableDefinition.setDescription(element.attributeValue(DESCRIPTION));
        String userTypeName = element.attributeValue(USER_TYPE);
        if (userTypeName != null) {
            variableDefinition.setFormat(userTypeName);
        } else {
            String format = element.attributeValue(FORMAT);
            format = BackCompatibilityClassNames.getClassName(format);
            variableDefinition.setFormat(format);
            String formatLabel;
            if (format.contains(VariableDefinition.FORMAT_COMPONENT_TYPE_START)) {
                formatLabel = localizationDAO.getLocalized(variableDefinition.getFormatClassName());
                formatLabel += VariableDefinition.FORMAT_COMPONENT_TYPE_START;
                String[] componentClassNames = variableDefinition.getFormatComponentClassNames();
                for (int i = 0; i < componentClassNames.length; i++) {
                    if (i != 0) {
                        formatLabel += VariableDefinition.FORMAT_COMPONENT_TYPE_CONCAT;
                    }
                    formatLabel += localizationDAO.getLocalized(componentClassNames[i]);
                }
                formatLabel += VariableDefinition.FORMAT_COMPONENT_TYPE_END;
            } else {
                formatLabel = localizationDAO.getLocalized(format);
            }
            variableDefinition.setFormatLabel(formatLabel);
        }
        variableDefinition.setPublicAccess(Boolean.parseBoolean(element.attributeValue(PUBLIC, "false")));
        String stringDefaultValue = element.attributeValue(DEFAULT_VALUE);
        if (!Strings.isNullOrEmpty(stringDefaultValue)) {
            try {
                VariableFormat variableFormat = FormatCommons.create(variableDefinition);
                Object value = variableFormat.parse(stringDefaultValue);
                variableDefinition.setDefaultValue(value);
            } catch (Exception e) {
                LogFactory.getLog(getClass()).error("Unable to format default value '" + name + "' in " + processDefinition, e);
            }
        }
        variableDefinition.setUserTypes(userTypes);
        return variableDefinition;
    }

}

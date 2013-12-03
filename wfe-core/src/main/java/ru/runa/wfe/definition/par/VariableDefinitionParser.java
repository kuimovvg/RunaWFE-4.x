package ru.runa.wfe.definition.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;

@SuppressWarnings("unchecked")
public class VariableDefinitionParser implements ProcessArchiveParser {
    @Autowired
    private LocalizationDAO localizationDAO;

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return false;
    }

    @Override
    public void readFromArchive(ProcessArchive archive, ProcessDefinition processDefinition) {
        byte[] xml = archive.getFileDataNotNull(IFileDataProvider.VARIABLES_XML_FILE_NAME);
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        List<Element> elements = root.elements("variable");
        for (Element element : elements) {
            String name = element.attributeValue("name");
            String scriptingName = element.attributeValue("scriptingName", name);
            boolean swimlane = Boolean.parseBoolean(element.attributeValue("swimlane", "false"));
            if (swimlane) {
                processDefinition.getSwimlane(name).setScriptingName(scriptingName);
            } else {
                String format = element.attributeValue("format");
                format = BackCompatibilityClassNames.getClassName(format);
                VariableDefinition variable = new VariableDefinition(false, name, format, scriptingName);
                String formatLabel;
                if (format.contains(VariableDefinition.FORMAT_COMPONENT_TYPE_START)) {
                    formatLabel = localizationDAO.getLocalized(variable.getFormatClassName());
                    formatLabel += VariableDefinition.FORMAT_COMPONENT_TYPE_START;
                    String[] componentClassNames = variable.getFormatComponentClassNames();
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
                variable.setFormatLabel(formatLabel);
                variable.setPublicAccess(Boolean.parseBoolean(element.attributeValue("public", "false")));
                variable.setDefaultValue(element.attributeValue("defaultValue"));
                processDefinition.addVariable(variable.getName(), variable);
            }
        }
    }

}
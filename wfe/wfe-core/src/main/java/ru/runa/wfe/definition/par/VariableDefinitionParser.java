package ru.runa.wfe.definition.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;

public class VariableDefinitionParser implements ProcessArchiveParser {
    @Autowired
    private LocalizationDAO localizationDAO;

    @Override
    public void readFromArchive(ProcessArchive archive, ProcessDefinition processDefinition) {
        byte[] xml = archive.getFileDataNotNull(IFileDataProvider.VARIABLES_XML_FILE_NAME);
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        List<Element> elements = root.elements("variable");
        for (Element element : elements) {
            VariableDefinition variable = new VariableDefinition();
            variable.setName(element.attributeValue("name"));
            variable.setFormat(element.attributeValue("format"));
            variable.setPublicAccess(Boolean.parseBoolean(element.attributeValue("public", "false")));
            variable.setDefaultValue(element.attributeValue("defaultValue"));
            variable.setDisplayFormat(localizationDAO.getLocalized(variable.getFormat()));
            processDefinition.addVariable(variable.getName(), variable);
        }
    }

}

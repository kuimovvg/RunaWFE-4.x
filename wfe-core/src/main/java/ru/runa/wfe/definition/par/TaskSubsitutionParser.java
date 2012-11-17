package ru.runa.wfe.definition.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.jpdl.JpdlProcessArchive;
import ru.runa.wfe.lang.ProcessDefinition;

public class TaskSubsitutionParser implements ProcessArchiveParser {

    @Override
    public void readFromArchive(JpdlProcessArchive archive, final ProcessDefinition processDefinition) {
        byte[] xml = processDefinition.getFileData(IFileDataProvider.SUBSTITUTION_EXCEPTIONS_FILE_NAME);
        if (xml == null) {
            return;
        }
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        List<Element> elements = root.elements("task");
        for (Element element : elements) {
            String nodeId = element.attributeValue("name");
            processDefinition.addTaskNameToignoreSubstitutionRules(nodeId);
        }
    }

}

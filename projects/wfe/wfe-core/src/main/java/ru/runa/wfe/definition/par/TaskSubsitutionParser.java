package ru.runa.wfe.definition.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.lang.ProcessDefinition;

/**
 * @deprecated remove in 4.2.0
 */
public class TaskSubsitutionParser implements ProcessArchiveParser {

    @Override
    public void readFromArchive(ProcessArchive archive, final ProcessDefinition processDefinition) {
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

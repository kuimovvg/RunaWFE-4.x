package ru.runa.wfe.definition.par;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.jpdl.JpdlProcessArchive;
import ru.runa.wfe.lang.ProcessDefinition;

public class MappingsParser implements ProcessArchiveParser {
    private static final Log log = LogFactory.getLog(MappingsParser.class);

    @Override
    public void readFromArchive(JpdlProcessArchive archive, ProcessDefinition processDefinition) {
        byte[] xml = archive.getFileData(IFileDataProvider.MAPPINGS_XML_FILE_NAME);
        if (xml == null) {
            log.warn("No '" + IFileDataProvider.MAPPINGS_XML_FILE_NAME + "' in " + processDefinition);
            return;
        }
        try {
            Document document = XmlUtils.parseWithoutValidation(xml);
            Element root = document.getRootElement();
            List<Element> elements = root.elements("mapping");
            for (Element element : elements) {
                processDefinition.getDisplayMappings().put(element.attributeValue("format"), element.attributeValue("name"));
            }
        } catch (Exception e) {
            log.error("Unable parse " + IFileDataProvider.MAPPINGS_XML_FILE_NAME, e);
        }
    }

}

package ru.runa.wfe.definition.par;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.bpmn.BpmnXmlReader;
import ru.runa.wfe.definition.jpdl.JpdlXmlReader;
import ru.runa.wfe.lang.ProcessDefinition;

public class ProcessDefinitionParser implements ProcessArchiveParser {

    @Override
    public void readFromArchive(ProcessArchive processArchive, ProcessDefinition processDefinition) {
        byte[] definitionXml = processArchive.getFileDataNotNull(IFileDataProvider.PROCESSDEFINITION_XML_FILE_NAME);
        Document document = XmlUtils.parseWithoutValidation(definitionXml);
        Element root = document.getRootElement();
        if ("process-definition".equals(root.getName())) {
            JpdlXmlReader reader = ApplicationContextFactory.autowireBean(new JpdlXmlReader(document));
            reader.readProcessDefinition(processDefinition);
        } else if ("definitions".equals(root.getName())) {
            BpmnXmlReader reader = ApplicationContextFactory.autowireBean(new BpmnXmlReader(document));
            reader.readProcessDefinition(processDefinition);
        } else {
            throw new InternalApplicationException("Couldn't determine language from content");
        }
    }
}

package ru.runa.gpd.lang;

import org.dom4j.Document;

public class BpmnSerializer extends JpdlSerializer {
    @Override
    public boolean isSupported(Document document) {
        if ("".equals(document.getRootElement().getName())) {
            return Language.BPMN.name().equals(document.getRootElement().attributeValue("lang"));
        }
        return false;
    }

    @Override
    public Document getInitialProcessDefinitionDocument(String processName) {
        Document document = super.getInitialProcessDefinitionDocument(processName);
        document.getRootElement().addAttribute("lang", Language.BPMN.name());
        return document;
    }
}

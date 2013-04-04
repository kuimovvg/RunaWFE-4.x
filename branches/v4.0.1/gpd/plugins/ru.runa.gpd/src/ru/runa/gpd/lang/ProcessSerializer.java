package ru.runa.gpd.lang;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;

import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class ProcessSerializer {
    public abstract boolean isSupported(Document document);

    public abstract Document getInitialProcessDefinitionDocument(String processName, Map<String, String> properties);

    public abstract ProcessDefinition parseXML(Document document);

    public abstract void saveToXML(ProcessDefinition definition, Document document);

    public abstract void validateProcessDefinitionXML(IFile file);

    protected void setAttribute(Element node, String attributeName, String attributeValue) {
        if (attributeValue != null) {
            node.addAttribute(attributeName, attributeValue);
        }
    }

    protected void setNodeValue(Element node, String nodeValue) {
        if (nodeValue != null) {
            node.addCDATA(nodeValue);
        }
    }
}

package org.jbpm.ui;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class JpdlSerializer {
    protected String jpdlVersion;

    public void setJpdlVersion(String jpdlVersion) {
        this.jpdlVersion = jpdlVersion;
    }

    public abstract boolean isSupported(Document document);

    public abstract Document getInitialProcessDefinitionDocument(String processName) throws ParserConfigurationException;

    public abstract ProcessDefinition parseXML(Document document);

    public abstract void saveToXML(ProcessDefinition definition, Document document);

    public abstract void validateProcessDefinitionXML(IFile file) throws SAXException;

    protected String getAttribute(Node node, String name) {
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr != null) {
            return attr.getNodeValue();
        }
        return null;
    }

    protected String getTextContent(Node node) {
        if (node.getChildNodes().getLength() == 1) {
            return node.getFirstChild().getNodeValue();
        }
        return null;
    }

    protected void setAttribute(Element node, String attributeName, String attributeValue) {
        if (attributeValue != null) {
            node.setAttribute(attributeName, attributeValue);
        }
    }

    protected void setNodeValue(Element node, String nodeValue) {
        if (nodeValue != null) {
            CDATASection section = node.getOwnerDocument().createCDATASection(nodeValue);
            node.appendChild(section);
        }
    }

}

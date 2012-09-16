package ru.runa.bpm.ui.par;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AuxContentProvider {
    protected static final String NAME_ATTRIBUTE_NAME = "name";

    public abstract void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception;

    public abstract void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception;

    protected void updateFile(IFile file, byte[] contentBytes) throws CoreException {
        InputStream content = new ByteArrayInputStream(contentBytes);
        if (!file.exists()) {
            file.create(content, true, null);
        } else {
            file.setContents(content, true, true, null);
        }
    }

    protected void deleteFile(IFile file) throws CoreException {
        if (file.exists()) {
            file.delete(true, null);
        }
    }

    protected Element addElement(Element element, String elementName) {
        Element child = element.getOwnerDocument().createElement(elementName);
        element.appendChild(child);
        return child;
    }

    protected void addAttribute(Element e, String name, String value) {
        if (value != null) {
            e.setAttribute(name, value);
        }
    }

    protected int getIntAttribute(Element e, String name, int defaultValue) {
        String attrValue = e.getAttribute(name);
        if (isEmptyOrNull(attrValue)) {
            return defaultValue;
        }
        return Integer.valueOf(attrValue);
    }

    protected boolean getBooleanAttribute(Element e, String name, boolean defaultValue) {
        String attrValue = e.getAttribute(name);
        if (isEmptyOrNull(attrValue)) {
            return defaultValue;
        }
        return Boolean.valueOf(attrValue);
    }

    protected String getAttribute(Element e, String name, String defaultValue) {
        String attrValue = e.getAttribute(name);
        if (isEmptyOrNull(attrValue)) {
            return defaultValue;
        }
        return attrValue;
    }

    protected boolean isEmptyOrNull(String str) {
        return (str == null || str.length() == 0);
    }

    protected List<Element> getNamedChildren(Node node, String name) {
        List<Element> result = new ArrayList<Element>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element && name.equals(child.getNodeName())) {
                Element element = (Element) child;
                result.add(element);
            }
        }
        return result;
    }

}

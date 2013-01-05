package ru.runa.gpd.lang.par;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.lang.model.ProcessDefinition;

import com.google.common.base.Strings;

public abstract class AuxContentProvider {
    protected static final String NAME_ATTRIBUTE_NAME = "name";

    public abstract void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception;

    public abstract void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception;

    protected void updateFile(IFile file, byte[] contentBytes) throws CoreException {
        if (contentBytes != null) {
            InputStream content = new ByteArrayInputStream(contentBytes);
            if (!file.exists()) {
                file.create(content, true, null);
            } else {
                file.setContents(content, true, true, null);
            }
        } else {
            if (file.exists()) {
                file.delete(true, null);
            }
        }
    }

    protected void deleteFile(IFile file) throws CoreException {
        if (file.exists()) {
            file.delete(true, null);
        }
    }

    protected void addAttribute(Element e, String name, String value) {
        if (value != null) {
            e.addAttribute(name, value);
        }
    }

    protected int getIntAttribute(Element e, String name, int defaultValue) {
        String attrValue = e.attributeValue(name);
        if (Strings.isNullOrEmpty(attrValue)) {
            return defaultValue;
        }
        return Integer.valueOf(attrValue);
    }

    protected boolean getBooleanAttribute(Element e, String name, boolean defaultValue) {
        String attrValue = e.attributeValue(name);
        if (Strings.isNullOrEmpty(attrValue)) {
            return defaultValue;
        }
        return Boolean.valueOf(attrValue);
    }
}

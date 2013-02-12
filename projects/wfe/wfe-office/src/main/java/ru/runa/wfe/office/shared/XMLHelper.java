package ru.runa.wfe.office.shared;

import org.dom4j.Element;

import com.google.common.base.Preconditions;

public class XMLHelper {

    public static int getIntAttribute(Element element, String attributeName) {
        String string = element.attributeValue(attributeName);
        Preconditions.checkNotNull(string);
        return new Integer(string);
    }

    public static int getIntAttribute(Element element, String attributeName, int defaultValue) {
        String string = element.attributeValue(attributeName);
        if (string == null) {
            return defaultValue;
        }
        return new Integer(string);
    }

}

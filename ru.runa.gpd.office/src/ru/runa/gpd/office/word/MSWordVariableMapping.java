package ru.runa.gpd.office.word;

import java.util.Observable;

import org.dom4j.Element;

public class MSWordVariableMapping extends Observable {
    private String variableName = "";
    private String bookmarkName = "";
    private String formatClassName;

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName, String format) {
        this.variableName = variableName;
        if ("time".equals(format)) {
            formatClassName = "ru.runa.wf.web.forms.format.TimeFormat";
        } else if ("date".equals(format)) {
            formatClassName = "ru.runa.wf.web.forms.format.DateFormat";
        } else if ("datetime".equals(format)) {
            formatClassName = "ru.runa.wf.web.forms.format.DateTimeFormat";
        } else {
            formatClassName = null;
        }
    }

    public String getBookmarkName() {
        return bookmarkName;
    }

    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    public void serialize(Element parent) {
        Element element = parent.addElement("mapping");
        element.addAttribute("variable", variableName);
        element.addAttribute("bookmark", bookmarkName);
        if (formatClassName != null) {
            element.addAttribute("format-class", formatClassName);
        }
    }

    public static MSWordVariableMapping deserialize(Element element) {
        MSWordVariableMapping model = new MSWordVariableMapping();
        model.variableName = element.attributeValue("variable");
        model.bookmarkName = element.attributeValue("bookmark");
        String f = element.attributeValue("format-class");
        if (f != null && f.length() > 0) {
            model.formatClassName = f;
        }
        return model;
    }
}

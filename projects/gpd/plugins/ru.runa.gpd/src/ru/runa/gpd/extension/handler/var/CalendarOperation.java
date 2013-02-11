package ru.runa.gpd.extension.handler.var;

import java.util.Observable;

import org.dom4j.Element;

import com.google.common.base.Strings;

public class CalendarOperation extends Observable {
    public static final String ADD = "+";
    public static final String SET = "=";
    private String fieldName = "";
    private String expression = "";
    private String type = "";

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String field) {
        this.fieldName = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void serialize(Element parent) {
        Element element = parent.addElement("operation");
        element.addAttribute("type", type);
        element.addAttribute("field", CalendarConfig.CALENDAR_FIELDS.get(fieldName).toString());
        element.addAttribute("expression", expression);
    }

    public static CalendarOperation deserialize(Element element) {
        CalendarOperation model = new CalendarOperation();
        model.type = element.attributeValue("type");
        String fieldString = element.attributeValue("field");
        if (!Strings.isNullOrEmpty(fieldString)) {
            int field = Integer.parseInt(fieldString);
            model.fieldName = CalendarConfig.getFieldName(field);
        }
        model.expression = element.attributeValue("expression");
        return model;
    }
}

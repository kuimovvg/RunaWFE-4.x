package ru.runa.bpm.ui.custom;

import java.util.HashSet;
import java.util.Set;

import org.dom4j.Element;
import ru.runa.bpm.ui.resource.Messages;

public class ParamDef {

    public static final int TYPE_AUTO = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_COMBO = 2;
    public static final int TYPE_CHECKBOX = 3;

    public static final int XML_TYPE_ATTR = 1;
    public static final int XML_TYPE_NODE = 2;

    private int type = TYPE_AUTO;
    private int xmlNodeType = XML_TYPE_ATTR;
    private final String name;
    private final String label;
    private final Set<String> formatFilters = new HashSet<String>();
    private String help;
    private String[] comboItems = {};
    private String defaultValue;

    private boolean useVariable = true;
    private boolean optional = false;

    public ParamDef(Element element) {
        if (element.attributeValue("type") != null) {
            this.type = Integer.parseInt(element.attributeValue("type"));
        }
        if (element.attributeValue("xmlNodeType") != null) {
            this.xmlNodeType = Integer.parseInt(element.attributeValue("xmlNodeType"));
        }
        if (element.attributeValue("variable") != null) {
            this.useVariable = Boolean.parseBoolean(element.attributeValue("variable"));
        }
        this.name = element.attributeValue("name");
        if (element.attributeValue("label.key") != null) {
            this.label = Messages.getString(element.attributeValue("label.key"));
        } else {
            this.label = element.attributeValue("label");
        }
        this.help = element.attributeValue("help");
        if (element.attributeValue("help.key") != null) {
            this.help = Messages.getString(element.attributeValue("help.key"));
        }
        if (element.attributeValue("optional") != null) {
            this.optional = Boolean.parseBoolean(element.attributeValue("optional"));
        }
        if (element.attributeValue("options") != null) {
            this.comboItems = element.attributeValue("options").split(",", -1);
        }
        this.defaultValue = element.attributeValue("defaultValue");
        String formatFilter = element.attributeValue("formatFilter");
        if (formatFilter != null && formatFilter.length() > 0) {
            String[] formats = formatFilter.split(";", -1);
            for (String format : formats) {
                formatFilters.add(format);
            }
        }
    }

    public ParamDef(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public int determineType() {
        if (TYPE_AUTO == type) {
            return (useVariable || comboItems.length > 0) ? TYPE_COMBO : TYPE_TEXT;
        }
        return type;
    }

    public int getXmlNodeType() {
        return xmlNodeType;
    }

    public void setXmlNodeType(int xmlNodeType) {
        this.xmlNodeType = xmlNodeType;
    }

    public String[] getComboItems() {
        return comboItems;
    }

    public Set<String> getFormatFilters() {
        return formatFilters;
    }

    public String getHelp() {
        return help;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public boolean isUseVariable() {
        return useVariable;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setComboItems(String[] comboItems) {
        this.comboItems = comboItems;
    }

    public void setUseVariable(boolean useVariable) {
        this.useVariable = useVariable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

}

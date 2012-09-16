package ru.runa.bpm.ui.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

public class ParamDefGroup {
    public static final String NAME_INPUT = "input";
    public static final String NAME_OUTPUT = "output";
    public static final String LABEL_EMPTY = "-";
    
    private final String name;
    private String label = LABEL_EMPTY;
    private String help;
    private final List<ParamDef> parameters = new ArrayList<ParamDef>();
    private final Map<String, String> dynaProperties = new HashMap<String, String>();
    
    public ParamDefGroup(String name) {
        this.name = name;
    }
    
    public ParamDefGroup(Element element) {
        this.name = element.getName();
        setLabel(element.attributeValue("label"));
    }
    
    public void setLabel(String label) {
        if (label != null) {
            this.label = label;
        }
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
    
    public void setHelp(String help) {
        this.help = help;
    }

    public List<ParamDef> getParameters() {
        return parameters;
    }
    
    public Map<String, String> getDynaProperties() {
        return dynaProperties;
    }
    
}

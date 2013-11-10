package ru.runa.gpd.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import ru.runa.gpd.extension.VariableFormatRegistry;

public class ValidatorDefinition {
    public static final String REQUIRED_VALIDATOR_NAME = "required";
    public static final String GLOBAL_VALIDATOR_NAME = "expression";
    public static final String EXPRESSION_PARAM_NAME = "expression";
    public static final String GLOBAL_TYPE = "global";
    public static final String FIELD_TYPE = "field";
    private final String name;
    private final String label;
    private final String type;
    private final List<String> applicable = new ArrayList<String>();
    private final String description;
    private final Map<String, Param> params = new HashMap<String, Param>();

    public ValidatorDefinition(String name, String label, String type, String description) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.description = description;
    }

    public void addApplicableType(String applicableType) {
        this.applicable.add(applicableType);
    }

    public ValidatorConfig create(String errorMessage) {
        ValidatorConfig config = new ValidatorConfig(name, errorMessage, new HashMap<String, String>());
        return config;
    }

    public boolean isGlobal() {
        return GLOBAL_TYPE.equals(type);
    }

    public boolean isDefault() {
        return REQUIRED_VALIDATOR_NAME.equals(name);
    }

    public boolean isApplicable(String className) {
        if (applicable.isEmpty()) {
            return true;
        }
        for (String appClassName : applicable) {
            if (VariableFormatRegistry.isAssignableFrom(appClassName, className)) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        return description;
    }

    public void addParam(Param param) {
        params.put(param.getName(), param);
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<String, Param> getParams() {
        return params;
    }

    public void writeConfig(Element parentElement, ValidatorConfig config) {
        if (!name.equals(config.getType())) {
            throw new IllegalArgumentException("Invalid type: " + config.getType() + " for vd: " + name);
        }
        String validatorTag;
        if (FIELD_TYPE.equals(type)) {
            validatorTag = "field-validator";
        } else if (GLOBAL_TYPE.equals(type)) {
            validatorTag = "validator";
        } else {
            throw new IllegalArgumentException("Unknown definition type: " + type);
        }
        Element validatorElement = parentElement.addElement(validatorTag);
        validatorElement.addAttribute("type", name);
        Element messageElement = validatorElement.addElement("message");
        messageElement.addCDATA(config.getMessage());
        for (String paramName : config.getParams().keySet()) {
            String paramValue = config.getParams().get(paramName);
            if ((paramValue != null) && (paramValue.length() > 0)) {
                Param param = params.get(paramName);
                if (param == null) {
                    throw new NullPointerException("Parameter not registered in validator definition: " + paramName);
                }
                Element paramElement = validatorElement.addElement("param");
                paramElement.addAttribute("name", paramName);
                paramElement.addCDATA(paramValue);
            }
        }
    }

    public static class Param {
        public static final String STRING_TYPE = String.class.getName();
        private final String name;
        private final String label;
        private final String type;

        public Param(String name, String label, String type) {
            this.name = name;
            this.label = label;
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}

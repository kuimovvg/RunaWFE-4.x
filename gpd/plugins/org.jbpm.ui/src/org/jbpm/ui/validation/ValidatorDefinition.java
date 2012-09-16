package ru.runa.bpm.ui.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.bpm.ui.util.XmlUtil;

public class ValidatorDefinition {
    public static final String REQUIRED_VALIDATOR_NAME = "required";
    public static final String GLOBAL_VALIDATOR_NAME = "expression";
    public static final String EXPRESSION_PARAM_NAME = "expression";

    public static final String GLOBAL_TYPE = "global";
    public static final String FIELD_TYPE = "field";

    private final String name;

    private final String displayName;

    private final String type;

    private final List<String> applicable = new ArrayList<String>();

    private final String description;

    private final Map<String, Param> params = new HashMap<String, Param>();

    public ValidatorDefinition(String name, String displayName, String type, String description) {
        this.name = name;
        this.displayName = displayName;
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

    public boolean isApplicable(String formatName) {
        if (applicable.isEmpty()) {
            return true;
        }
        return applicable.contains(formatName);
    }

    public String getDescription() {
        return description;
    }

    public void addParam(Param param) {
        params.put(param.getName(), param);
    }

    public String getDisplayName() {
        return displayName;
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

    public String formatConfig(ValidatorConfig config) {
        if (!name.equals(config.getType())) {
            throw new IllegalArgumentException("Invalid type: " + config.getType() + " for vd: " + name);
        }
        StringBuffer buffer = new StringBuffer();
        String validatorTag;
        if (FIELD_TYPE.equals(type)) {
            validatorTag = "field-validator";
        } else if (GLOBAL_TYPE.equals(type)) {
            validatorTag = "validator";
        } else {
            throw new IllegalArgumentException("Unknown definition type: " + type);
        }
        buffer.append("\n<").append(validatorTag).append(" type=\"").append(name).append("\">");
        buffer.append("\n\t<message>").append(XmlUtil.normalize(config.getMessage())).append("</message>");
        for (String paramName : config.getParams().keySet()) {
            String paramValue = config.getParams().get(paramName);
            if ((paramValue != null) && (paramValue.length() > 0)) {
                Param param = params.get(paramName);
                if (param == null) {
                    throw new NullPointerException("Parameter not registered in validator definition: " + paramName);
                }
                paramValue = XmlUtil.normalize(paramValue);
                buffer.append("\n\t<param name=\"").append(paramName).append("\">").append(paramValue).append("</param>");
            }
        }

        buffer.append("\n</").append(validatorTag).append(">");
        return buffer.toString();
    }

    public static class Param {
        public static final String STRING_TYPE = "string";

        public static final String NUMBER_TYPE = "number";

        public static final String BOOLEAN_TYPE = "boolean";

        public static final String REGEX_TYPE = "regex";

        public static final String BSH_TYPE = "bshCode";

        public static final String DATE_TYPE = "date";

        public static final String TIME_TYPE = "time";

        private final String name;

        private final String displayName;

        private final String type;

        public Param(String name, String displayName, String type) {
            this.name = name;
            this.displayName = displayName;
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

}

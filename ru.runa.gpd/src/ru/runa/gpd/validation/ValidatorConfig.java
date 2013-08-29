package ru.runa.gpd.validation;

import java.util.Map;

public class ValidatorConfig {
    public static final String GLOBAL_FIELD_ID = "";

    private String type;

    private String message;

    private Map<String, String> params;

    public ValidatorConfig(String validatorType, String message, Map<String, String> params) {
        this.type = validatorType;
        this.message = message;
        this.params = params;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getType() {
        return type;
    }

    public boolean check() {
        if ("expression".equals(type) && params.get("expression") == null) {
            return false;
        }
        return true;
    }
}

package ru.runa.bpm.ui.infopath;

import ru.runa.bpm.ui.util.ValidationUtil;
import ru.runa.bpm.ui.validation.ValidatorConfig;
import ru.runa.bpm.ui.validation.ValidatorDefinition;

public class StringLengthExpressionAdapter implements ExpressionAdapter {

    public ValidatorConfig createConfig(String expr, String errorMessage) throws Exception {
        ValidatorDefinition definition = ValidationUtil.getValidatorDefinition("stringlength");
        ValidatorConfig config = definition.create(errorMessage);

        String argName;
        int index;
        if (expr.indexOf("<") > 0) {
            argName = "maxLength";
            index = expr.indexOf("<");
        } else if (expr.indexOf(">") > 0) {
            argName = "minLength";
            index = expr.indexOf(">");
        } else {
            throw new Exception("Unknown expr: " + expr);
        }
        String argValue = expr.substring(index + 2);
        config.getParams().put(argName, argValue);
        return config;
    }

}

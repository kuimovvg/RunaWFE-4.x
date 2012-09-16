package ru.runa.bpm.ui.infopath;

import ru.runa.bpm.ui.util.ValidationUtil;
import ru.runa.bpm.ui.validation.ValidatorConfig;
import ru.runa.bpm.ui.validation.ValidatorDefinition;

public class RequiredExpressionAdapter implements ExpressionAdapter {

    public ValidatorConfig createConfig(String expr, String errorMessage) throws Exception {
        return ValidationUtil.getValidatorDefinition(ValidatorDefinition.REQUIRED_VALIDATOR_NAME).create(errorMessage);
    }

}

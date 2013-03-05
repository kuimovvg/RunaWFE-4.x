/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.wfe.validation;

import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Base class for field validators.
 */
public abstract class FieldValidator extends Validator {
    private String fieldName;

    @Override
    public void init(User user, ValidatorConfig config, ValidatorContext validatorContext, IVariableProvider variableProvider) {
        super.init(user, config, validatorContext, variableProvider);
        fieldName = config.getParams().get("fieldName");
    }

    public String getFieldName() {
        return fieldName;
    }

    protected Object getFieldValue() {
        return getVariableProvider().getValue(fieldName);
    }

    @Override
    protected void addError(String userMessage) {
        getValidatorContext().addFieldError(fieldName, userMessage);
    }

}

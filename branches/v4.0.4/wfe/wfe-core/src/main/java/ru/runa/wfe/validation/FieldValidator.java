/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.wfe.validation;

import java.util.Map;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Strings;

/**
 * Base class for field validators.
 */
public abstract class FieldValidator extends Validator {
    private String fieldName;
    private Object fieldValue;

    @Override
    public void init(User user, ProcessDefinition processDefinition, ValidatorConfig config, ValidatorContext validatorContext,
            Map<String, Object> variables, IVariableProvider variableProvider) {
        super.init(user, processDefinition, config, validatorContext, variables, variableProvider);
        fieldName = config.getParams().get("fieldName");
        fieldValue = variables.get(fieldName);
    }

    public String getFieldName() {
        return fieldName;
    }

    protected Object getFieldValue() {
        return fieldValue;
    }

    @Override
    protected void addError(String userMessage) {
        if (Strings.isNullOrEmpty(userMessage)) {
            userMessage = "no validation message";
        }
        getValidatorContext().addFieldError(fieldName, userMessage);
    }

}

/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.validators;

import ru.runa.commons.validation.FieldValidator;


/**
 * Base class for field validators.
 */
public abstract class FieldValidatorSupport extends ValidatorSupport implements FieldValidator {
    private String fieldName;
    private String type;

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

	protected Object getFieldValue() {
        return validatorContext.getVariable(fieldName);
    }

    protected void addFieldError() {
        validatorContext.addFieldError(fieldName, getMessage());
    }

    @Override
    public void setValidatorType(String type) {
        this.type = type;
    }

    @Override
    public String getValidatorType() {
        return type;
    }
}

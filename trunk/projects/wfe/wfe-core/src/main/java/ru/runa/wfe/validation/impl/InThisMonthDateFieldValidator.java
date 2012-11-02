package ru.runa.wfe.validation.impl;

import java.util.Calendar;
import java.util.Date;

import ru.runa.wfe.commons.TypeConversionUtil;

public class InThisMonthDateFieldValidator extends FieldValidatorSupport {

    @Override
    public void validate() {
        Date checkValue = TypeConversionUtil.convertTo(getFieldValue(), Date.class);
        if (checkValue == null) {
            // Use required validator for this
            return;
        }
        Calendar current = Calendar.getInstance();
        Calendar check = Calendar.getInstance();
        check.setTime(checkValue);
        if (current.get(Calendar.MONTH) != check.get(Calendar.MONTH)) {
            addFieldError();
        }
    }

}

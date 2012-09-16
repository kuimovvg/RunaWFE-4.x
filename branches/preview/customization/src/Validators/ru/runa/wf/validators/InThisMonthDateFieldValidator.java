package ru.runa.wf.validators;

import java.util.Calendar;
import java.util.Date;

import ru.runa.commons.validation.ValidationException;
import ru.runa.validators.FieldValidatorSupport;


public class InThisMonthDateFieldValidator extends FieldValidatorSupport {

    public void validate() throws ValidationException {
        Date checkValue = (Date) getFieldValue();
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

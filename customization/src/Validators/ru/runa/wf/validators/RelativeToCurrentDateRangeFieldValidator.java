package ru.runa.wf.validators;

import java.util.Calendar;
import java.util.Date;

import ru.runa.commons.validation.ValidationException;
import ru.runa.validators.AbstractRangeValidator;


public class RelativeToCurrentDateRangeFieldValidator extends AbstractRangeValidator<Date> {

    public Date getParam(String name, boolean add) {
        Object obj = getParameter(name);
        if (obj == null) {
            return null;
        }
        int daysCount = Integer.parseInt((String) obj);
        if (!add) {
            daysCount = -1 * daysCount - 1;
        }

        Calendar current = Calendar.getInstance();
        current.add(Calendar.DAY_OF_MONTH, daysCount);
        return current.getTime();
    }

    @Override
    protected Date getMaxComparatorValue() throws ValidationException {
        return getParam("max", true);
    }

    @Override
    protected Date getMinComparatorValue() throws ValidationException {
        return getParam("min", false);
    }

}

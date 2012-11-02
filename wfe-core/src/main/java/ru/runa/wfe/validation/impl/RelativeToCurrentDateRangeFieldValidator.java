package ru.runa.wfe.validation.impl;

import java.util.Calendar;
import java.util.Date;

import ru.runa.wfe.commons.TypeConversionUtil;

public class RelativeToCurrentDateRangeFieldValidator extends AbstractRangeValidator<Date> {

    public Date getParam(String name, boolean add) {
        Integer daysCount = TypeConversionUtil.convertTo(getParameter(name), Integer.class);
        if (daysCount == null) {
            return null;
        }
        if (!add) {
            daysCount = -1 * daysCount - 1;
        }
        Calendar current = Calendar.getInstance();
        current.add(Calendar.DAY_OF_MONTH, daysCount);
        return current.getTime();
    }

    @Override
    protected Date getMaxComparatorValue() {
        return getParam("max", true);
    }

    @Override
    protected Date getMinComparatorValue() {
        return getParam("min", false);
    }

}

package ru.runa.wfe.validation.impl;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.calendar.BusinessCalendar;
import ru.runa.wfe.commons.calendar.impl.Duration;

public class RelativeToCurrentDateRangeFieldValidator extends AbstractRangeValidator<Date> {

    @Autowired
    private BusinessCalendar businessCalendar;

    private Date getParameter(String name, boolean add) {
        int daysCount = getParameter(int.class, name, 0);
        if (!add) {
            daysCount = -1 * daysCount - 1;
        }
        Calendar current = Calendar.getInstance();
        boolean useBusinessCalendar = getParameter(boolean.class, "useBusinessCalendar", false);
        if (useBusinessCalendar) {
            Date date = businessCalendar.add(new Date(), new Duration(daysCount + " business days"));
            current.setTime(date);
        } else {
            current.add(Calendar.DAY_OF_MONTH, daysCount);
        }
        return current.getTime();
    }

    @Override
    protected Date getMaxComparatorValue() {
        return getParameter("max", true);
    }

    @Override
    protected Date getMinComparatorValue() {
        return getParameter("min", false);
    }

}

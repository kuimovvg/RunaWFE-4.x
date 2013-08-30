package ru.runa.wfe.commons.bc;

import java.util.Calendar;
import java.util.Date;

/**
 * Refactored @since 4.1.0
 */
public interface BusinessCalendar {

    public Date add(Date date, String durationString);

    public boolean isHoliday(Calendar calendar);
}

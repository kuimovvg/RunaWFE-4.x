package ru.runa.wfe.commons.calendar;

import java.util.Date;

import ru.runa.wfe.commons.calendar.impl.Day;
import ru.runa.wfe.commons.calendar.impl.Duration;

/**
 * modified on 06.03.2009 by gavrusev_sergei
 * 
 * 
 */
public interface BusinessCalendar {
    // duration can be in business or in regular time
    Date add(Date date, Duration duration);

    // find the next working date with 00:00 time set
    Date findStartOfNextDay(Date date);

    // find the previous working date with 23:59:59:999 time set
    Date findEndOfPrevDay(Date date);

    // true if the working time for this date is 0 hours
    boolean isHoliday(Date date);

    public Day findDay(Date date);
}

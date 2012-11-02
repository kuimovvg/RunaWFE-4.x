package ru.runa.wfe.commons;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.runa.wfe.InternalApplicationException;

/**
 * Helper for {@link Calendar} and {@link CalendarInterval}.
 * 
 * @author dofs
 * @since 4.0
 */
public class CalendarUtil {
    public static final DateFormat DATE_WITHOUT_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    public static final DateFormat DATE_WITH_HOUR_MINUTES_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    public static final DateFormat DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public static final DateFormat HOURS_MINUTES_FORMAT = new SimpleDateFormat("HH:mm");
    public static final DateFormat HOURS_MINUTES_SECONDS_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private static final Calendar UNLIMITED_DATE = Calendar.getInstance();
    private static final Calendar ZERO_DATE = Calendar.getInstance();
    static {
        ZERO_DATE.set(Calendar.YEAR, 1900);
        UNLIMITED_DATE.set(Calendar.YEAR, 2100);
    }

    public static Calendar getZero() {
        return clone(ZERO_DATE);
    }

    public static Calendar getUnlimited() {
        return clone(UNLIMITED_DATE);
    }

    public static Calendar clone(Calendar calendar) {
        return (calendar == null) ? null : (Calendar) calendar.clone();
    }

    public static CalendarInterval clone(CalendarInterval interval) {
        if (interval == null) {
            return null;
        }
        return new CalendarInterval(clone(interval.getFrom()), clone(interval.getTo()));
    }

    public static Calendar getZeroTimeCalendar(Calendar calendar) {
        Calendar calendarClone = clone(calendar);
        CalendarUtil.setZeroTimeCalendar(calendarClone);
        return calendarClone;
    }

    public static void setZeroTimeCalendar(Calendar calendar) {
        if (calendar != null) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
    }

    public static void setZeroSecondsCalendar(Calendar calendar) {
        if (calendar != null) {
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
    }

    public static void setTimeFromCalendar(Calendar calendar, Calendar from) {
        if (calendar != null) {
            calendar.set(Calendar.HOUR_OF_DAY, from.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, from.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, from.get(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, from.get(Calendar.MILLISECOND));
        }
    }

    public static boolean isZeroTimeCalendar(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0
                && calendar.get(Calendar.MILLISECOND) == 0;
    }

    public static Calendar getLastSecondTimeCalendar(Calendar calendar) {
        Calendar calendarClone = clone(calendar);
        CalendarUtil.setLastSecondTimeCalendar(calendarClone);
        return calendarClone;
    }

    public static void setLastSecondTimeCalendar(Calendar calendar) {
        if (calendar == null) {
            return;
        }
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static boolean isLastSecondTimeCalendar(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) == 23 && calendar.get(Calendar.MINUTE) == 59 && calendar.get(Calendar.SECOND) == 59
                && calendar.get(Calendar.MILLISECOND) == 0;
    }

    public static String format(Date date, DateFormat dateFormat) {
        synchronized (dateFormat) {
            return date == null ? null : dateFormat.format(date);
        }
    }

    public static String formatDate(Date date) {
        return format(date, DATE_WITHOUT_TIME_FORMAT);
    }

    public static String formatDateTime(Date date) {
        return format(date, DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
    }

    public static String format(Calendar calendar, DateFormat dateFormat) {
        if (calendar == null) {
            return null;
        }
        return format(calendar.getTime(), dateFormat);
    }

    public static String formatDate(Calendar calendar) {
        return format(calendar, DATE_WITHOUT_TIME_FORMAT);
    }

    public static String formatDateTime(Calendar calendar) {
        return format(calendar, DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
    }

    public static Calendar dateToCalendar(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Calendar findMinCalendar(Calendar... calendars) {
        Calendar minCalendar = clone(UNLIMITED_DATE);
        for (Calendar calendar : calendars) {
            if (calendar != null && calendar.before(minCalendar)) {
                minCalendar = calendar;
            }
        }
        return minCalendar;
    }

    public static Calendar findMaxCalendar(Calendar... calendars) {
        Calendar maxCalendar = clone(ZERO_DATE);
        for (Calendar calendar : calendars) {
            if (calendar != null && calendar.after(maxCalendar)) {
                maxCalendar = calendar;
            }
        }
        return maxCalendar;
    }

    /**
     * 
     * @param one
     *            is a list of start - end pairs of calendar
     * @param two
     *            is a list of start - end pairs of calendar
     * @return 0 if no intersection. Returns N milliseconds of total intersection time
     */
    public static boolean isIntersectingStrong(Calendar oneStart, Calendar oneEnd, Calendar twoStart, Calendar twoEnd) {
        if (oneEnd.compareTo(twoStart) < 0 || twoEnd.compareTo(oneStart) < 0) {
            return false;
        }
        return true;
    }

    public static boolean isIntersectingWeak(Calendar oneStart, Calendar oneEnd, Calendar twoStart, Calendar twoEnd) {
        if (oneEnd.compareTo(twoStart) <= 0 || twoEnd.compareTo(oneStart) <= 0) {
            return false;
        }
        return true;
    }

    public static List<CalendarInterval> mergeIntersectingIntervalsNotOrdered(List<CalendarInterval> intervals) {
        if (intervals == null || intervals.size() < 2) {
            return intervals;
        }
        List<CalendarInterval> result = new ArrayList<CalendarInterval>();
        Collections.sort(intervals);
        CalendarInterval current = intervals.get(0);
        for (int i = 1; i < intervals.size(); i++) {
            if (isIntersectingStrong(current.getFrom(), current.getTo(), intervals.get(i).getFrom(), intervals.get(i).getTo())) {
                Calendar from = current.getFrom().after(intervals.get(i).getFrom()) ? intervals.get(i).getFrom() : current.getFrom();
                Calendar to = current.getTo().before(intervals.get(i).getTo()) ? intervals.get(i).getTo() : current.getTo();
                current = new CalendarInterval(from, to);
            } else {
                result.add(current);
                current = intervals.get(i);
            }
        }
        // add last element
        result.add(current);
        return result;
    }

    public static CalendarInterval mergeIntersectingIntervalsNotOrdered(CalendarInterval interval1, CalendarInterval interval2) {
        List<CalendarInterval> source = Arrays.asList(interval1, interval2);
        List<CalendarInterval> merged = mergeIntersectingIntervalsNotOrdered(source);
        if (merged.size() != 1) {
            throw new InternalApplicationException("Seems like intervals not intersecting " + source);
        }
        return merged.get(0);
    }

    public static List<Calendar> subtract(Calendar oneStart, Calendar oneEnd, Calendar twoStart, Calendar twoEnd) {
        ArrayList<Calendar> result = new ArrayList<Calendar>(4);
        if (!isIntersectingStrong(oneStart, oneEnd, twoStart, twoEnd)) {
            result.add((Calendar) oneStart.clone());
            result.add((Calendar) oneEnd.clone());
            return result;
        }
        Calendar current = null;
        if (oneStart.before(twoStart)) {
            current = (Calendar) oneStart.clone();
            result.add(current);
            current = (Calendar) twoStart.clone();
            result.add(current);
        }
        if (twoEnd.before(oneEnd)) {
            current = (Calendar) twoEnd.clone();
            result.add(current);
            current = (Calendar) oneEnd.clone();
            result.add(current);
        }
        if (oneStart.after(twoStart) && (twoEnd.after(oneEnd))) {
            return null;
        }
        result.trimToSize();
        return result;
    }

    public static List<CalendarInterval> subtract(List<CalendarInterval> subtractFrom, List<CalendarInterval> subtractThese) {
        List<Calendar> calendarsFrom = transformToCalendarList(subtractFrom);
        List<Calendar> calendarsThese = transformToCalendarList(subtractThese);
        List<Calendar> calendarsResult = subtractList(calendarsFrom, calendarsThese);
        return transformToCalendarIntervalList(calendarsResult);
    }

    private static List<Calendar> subtractList(List<Calendar> subtractFrom, List<Calendar> subtractThese) {
        if (subtractThese == null || subtractThese.size() == 0 || subtractFrom == null || subtractFrom.size() == 0) {
            return subtractFrom;
        }
        List<Calendar> current;
        ArrayList<Calendar> result = new ArrayList<Calendar>();
        result.addAll(subtractFrom);
        ArrayList<Calendar> temp = new ArrayList<Calendar>();
        for (int i = 0; i < subtractThese.size(); i = i + 2) {
            for (int j = 0; j < result.size(); j = j + 2) {
                current = subtract(result.get(j), result.get(j + 1), subtractThese.get(i), subtractThese.get(i + 1));
                if (current != null) {
                    temp.addAll(current);
                }
            }
            result.clear();
            result.addAll(temp);
            temp.clear();
        }
        result.trimToSize();
        return result;
    }

    public static void changeDateInCalendar(Calendar changeDateIn, Calendar takeDateFrom) {
        changeDateIn.set(takeDateFrom.get(Calendar.YEAR), takeDateFrom.get(Calendar.MONTH), takeDateFrom.get(Calendar.DAY_OF_MONTH));
    }

    public static int countMinutesFromMillis(long millis) {
        int result = (int) (millis / 60000);
        if (millis % 60000 > 30000) {
            result++;
        }
        return result;
    }

    public static int countCalendarIntervalsLength(List<CalendarInterval> calendarIntervals) {
        long resultInMillis = 0;
        for (CalendarInterval calendarInterval : calendarIntervals) {
            resultInMillis += calendarInterval.getLengthInMillis();
        }
        return countMinutesFromMillis(resultInMillis);
    }

    public static int compareHoursMinutes(Calendar time1, Calendar time2) {
        if (time1.get(Calendar.HOUR_OF_DAY) > time2.get(Calendar.HOUR_OF_DAY)) {
            return 1;
        } else if (time1.get(Calendar.HOUR_OF_DAY) < time2.get(Calendar.HOUR_OF_DAY)) {
            return -1;

        } else if (time1.get(Calendar.MINUTE) > time2.get(Calendar.MINUTE)) {
            return 1;
        } else if (time1.get(Calendar.MINUTE) < time2.get(Calendar.MINUTE)) {
            return -1;
        }
        return 0;
    }

    public static boolean areCalendarEqualsIgnoringMilliseconds(Calendar c1, Calendar c2) {
        c1 = clone(c1);
        c1.set(Calendar.MILLISECOND, 0);
        c2 = clone(c2);
        c2.set(Calendar.MILLISECOND, 0);
        return c1.equals(c2);
    }

    public static int compareOnlyDate(Calendar c1, Calendar c2) {
        if (c1 == null && c2 == null) {
            return 0;
        }
        if (c1 == null) {
            return -1;
        }
        if (c2 == null) {
            return 1;
        }
        Calendar d1 = getZeroTimeCalendar(c1);
        Calendar d2 = getZeroTimeCalendar(c2);
        return d1.compareTo(d2);
    }

    public static boolean areCalendarsEqualIgnoringTime(Calendar c1, Calendar c2) {
        return compareOnlyDate(c1, c2) == 0;
    }

    private static List<Calendar> transformToCalendarList(List<CalendarInterval> list) {
        ArrayList<Calendar> result = new ArrayList<Calendar>(2 * list.size());
        for (CalendarInterval calendarInterval : list) {
            result.add(calendarInterval.getFrom());
            result.add(calendarInterval.getTo());
        }
        return result;
    }

    private static List<CalendarInterval> transformToCalendarIntervalList(List<Calendar> list) {
        ArrayList<CalendarInterval> result = new ArrayList<CalendarInterval>(list.size() / 2);
        for (int i = 0; i < list.size(); i = i + 2) {
            result.add(new CalendarInterval(list.get(i), list.get(i + 1)));
        }
        return result;
    }

    public static double daysBetween(Calendar from, Calendar to) {
        if (to == null || from == null) {
            return 0;
        }
        long resultLong = to.getTimeInMillis() - from.getTimeInMillis();
        int minutes = countMinutesFromMillis(resultLong);
        return ((double) minutes / (24 * 60));
    }

    public static int minutesBetween(Calendar from, Calendar to) {
        if (to == null || from == null) {
            return 0;
        }
        long resultLong = to.getTimeInMillis() - from.getTimeInMillis();
        return countMinutesFromMillis(resultLong);
    }

    public static CalendarInterval convertToOrderedInterval(String date1, String date2, DateFormat dateFormat) {
        List<Calendar> calendarList = new ArrayList<Calendar>(2);
        calendarList.add(convertToCalendar(date1, dateFormat));
        calendarList.add(convertToCalendar(date2, dateFormat));
        Collections.sort(calendarList);
        CalendarInterval result = new CalendarInterval(calendarList.get(0), calendarList.get(1), true);
        return result;
    }

    public static synchronized Date convertToDate(String dateAsString, DateFormat dateFormat) {
        try {
            synchronized (dateFormat) {
                return dateFormat.parse(dateAsString);
            }
        } catch (ParseException e) {
            throw new InternalApplicationException("Unable parse " + dateAsString + " with format " + dateFormat, e);
        }
    }

    public static synchronized Calendar convertToCalendar(String dateAsString, DateFormat dateFormat) {
        return dateToCalendar(convertToDate(dateAsString, dateFormat));
    }

}

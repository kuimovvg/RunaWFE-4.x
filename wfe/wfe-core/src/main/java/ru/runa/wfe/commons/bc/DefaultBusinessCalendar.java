package ru.runa.wfe.commons.bc;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ru.runa.wfe.commons.CalendarInterval;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.PropertyResources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultBusinessCalendar extends AbstractBusinessCalendar {
    private static final PropertyResources RESOURCES = new PropertyResources("business.calendar.properties");
    private static final Map<Integer, BusinessDay> WEEK_DAYS = Maps.newHashMap();
    private static final List<Calendar> HOLIDAYS = Lists.newArrayList();
    static {
        WEEK_DAYS.put(Calendar.MONDAY, parse(RESOURCES.getStringProperty("weekday.monday", "")));
        WEEK_DAYS.put(Calendar.TUESDAY, parse(RESOURCES.getStringProperty("weekday.tuesday", "")));
        WEEK_DAYS.put(Calendar.WEDNESDAY, parse(RESOURCES.getStringProperty("weekday.wednesday", "")));
        WEEK_DAYS.put(Calendar.THURSDAY, parse(RESOURCES.getStringProperty("weekday.thursday", "")));
        WEEK_DAYS.put(Calendar.FRIDAY, parse(RESOURCES.getStringProperty("weekday.friday", "")));
        WEEK_DAYS.put(Calendar.SATURDAY, parse(RESOURCES.getStringProperty("weekday.saturday", "")));
        WEEK_DAYS.put(Calendar.SUNDAY, parse(RESOURCES.getStringProperty("weekday.sunday", "")));
        for (String propertyName : RESOURCES.getAllPropertyNames()) {
            if (propertyName.startsWith("holiday")) {
                Calendar calendar = CalendarUtil.convertToCalendar(RESOURCES.getStringProperty(propertyName), CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
                CalendarUtil.setZeroTimeCalendar(calendar);
                HOLIDAYS.add(calendar);
            }
        }
    }

    private static BusinessDay parse(String string) {
        List<CalendarInterval> workingIntervals = Lists.newArrayList();
        StringTokenizer tokenizer = new StringTokenizer(string, "&");
        while (tokenizer.hasMoreTokens()) {
            String dayPartText = tokenizer.nextToken().trim();
            int separatorIndex = dayPartText.indexOf('-');
            if (separatorIndex == -1) {
                throw new IllegalArgumentException("improper format of interval '" + dayPartText + "'");
            }
            String fromText = dayPartText.substring(0, separatorIndex).trim().toLowerCase();
            String toText = dayPartText.substring(separatorIndex + 1).trim().toLowerCase();
            Date from = CalendarUtil.convertToDate(fromText, CalendarUtil.HOURS_MINUTES_FORMAT);
            Date to = CalendarUtil.convertToDate(toText, CalendarUtil.HOURS_MINUTES_FORMAT);
            workingIntervals.add(new CalendarInterval(from, to));
        }
        return new BusinessDay(workingIntervals);
    }

    @Override
    protected BusinessDay getBusinessDay(Calendar calendar) {
        calendar = CalendarUtil.getZeroTimeCalendar(calendar);
        if (HOLIDAYS.contains(calendar)) {
            return BusinessDay.HOLIDAY;
        }
        return WEEK_DAYS.get(calendar.get(Calendar.DAY_OF_WEEK));
    }

}

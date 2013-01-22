/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.commons.calendar.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.calendar.BusinessCalendar;

/**
 * a calendar that knows about business hours. modified on 06.03.2009 by
 * gavrusev_sergei
 */
public class BusinessCalendarImpl implements BusinessCalendar, Serializable {
    private static final long serialVersionUID = 1L;
    private static Properties businessCalendarProperties = ClassLoaderUtil.getPropertiesNotNull("business.calendar.properties");

    private final Day[] weekDays;
    private final List<Holiday> holidays;

    public BusinessCalendarImpl() {
        weekDays = Day.parseWeekDays(businessCalendarProperties, this);
        holidays = Holiday.parseHolidays(businessCalendarProperties);
    }

    public static Properties getBusinessCalendarProperties() {
        return businessCalendarProperties;
    }

    @Override
    public Date add(Date date, Duration duration) {
        if (duration.getMilliseconds() >= 0) {
            return addForward(date, duration);
        } else {
            return addBack(date, duration);
        }
    }

    @Override
    public Date findStartOfNextDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        while (isHoliday(date)) {
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime();
        }
        return date;
    }

    @Override
    public Date findEndOfPrevDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        date = calendar.getTime();
        while (isHoliday(date)) {
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -1);
            date = calendar.getTime();
        }
        return date;
    }

    @Override
    public Day findDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return weekDays[calendar.get(Calendar.DAY_OF_WEEK)];
    }

    @Override
    public boolean isHoliday(Date date) {
        for (Holiday holiday : holidays) {
            if (holiday.includes(date)) {
                return true;
            }
        }
        return false;
    }

    DayPart findDayPart(Date date) {
        if (isHoliday(date)) {
            return null;
        }
        Day day = findDay(date);
        for (DayPart dayPart : day.dayParts) {
            if (dayPart.includes(date)) {
                return dayPart;
            }
        }
        return null;
    }

    public DayPart findNextDayPart(Date date) {
        DayPart nextDayPart = null;
        while (nextDayPart == null) {
            nextDayPart = findDayPart(date);
            if (nextDayPart != null) {
                break;
            }
            date = findStartOfNextDay(date);
            Day day = findDay(date);
            nextDayPart = day.findNextDayPartStart(0, date);
        }
        return nextDayPart;
    }

    @Override
    public boolean isInBusinessHours(Date date) {
        return (findDayPart(date) != null);
    }

    public static Calendar getCalendar() {
        return new GregorianCalendar();
    }

    public Date add(Date date, String duration) {
        throw new UnsupportedOperationException(BusinessCalendarImpl.class.getName());
    }

    private Date addForward(Date date, Duration duration) {
        if (!duration.isBusinessTime()) {
            return duration.addTo(date);
        }
        DayPart dayPart = findDayPart(date);
        if (dayPart != null) {
            return dayPart.add(date, duration);
        }
        dayPart = findDay(date).findNextDayPartStart(0, date);
        date = dayPart.getStartTime(date);
        return dayPart.add(date, duration);
    }

    private Date addBack(Date date, Duration duration) {
        Date end = null;
        if (duration.isBusinessTime()) {
            DayPart dayPart = findDayPart(date);
            if (dayPart == null) {
                Day day = findDay(date);
                dayPart = day.findPrevDayPartEnd(day.dayParts.length - 1, date);
                date = dayPart.getEndTime(date);
            }
            end = dayPart.add(date, duration);
        } else {
            end = duration.addTo(date);
        }
        return end;
    }
}

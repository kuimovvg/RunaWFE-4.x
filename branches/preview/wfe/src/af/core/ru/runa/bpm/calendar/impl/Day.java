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
package ru.runa.bpm.calendar.impl;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import ru.runa.bpm.calendar.BusinessCalendar;

/**
 * is a day on a business calendar.
 */
public class Day implements Serializable {

    private static final long serialVersionUID = 1L;

    public DayPart[] dayParts = null;
    BusinessCalendar businessCalendar = null;

    public static Day[] parseWeekDays(Properties calendarProperties, BusinessCalendar businessCalendar) {
        DateFormat dateFormat = new SimpleDateFormat(calendarProperties.getProperty("hour.format"));
        Day[] weekDays = new Day[8];
        weekDays[Calendar.MONDAY] = new Day(calendarProperties.getProperty("weekday.monday"), dateFormat, businessCalendar);
        weekDays[Calendar.TUESDAY] = new Day(calendarProperties.getProperty("weekday.tuesday"), dateFormat, businessCalendar);
        weekDays[Calendar.WEDNESDAY] = new Day(calendarProperties.getProperty("weekday.wednesday"), dateFormat, businessCalendar);
        weekDays[Calendar.THURSDAY] = new Day(calendarProperties.getProperty("weekday.thursday"), dateFormat, businessCalendar);
        weekDays[Calendar.FRIDAY] = new Day(calendarProperties.getProperty("weekday.friday"), dateFormat, businessCalendar);
        weekDays[Calendar.SATURDAY] = new Day(calendarProperties.getProperty("weekday.saturday"), dateFormat, businessCalendar);
        weekDays[Calendar.SUNDAY] = new Day(calendarProperties.getProperty("weekday.sunday"), dateFormat, businessCalendar);
        return weekDays;
    }

    public Day(String dayPartsText, DateFormat dateFormat, BusinessCalendar businessCalendar) {
        this.businessCalendar = businessCalendar;
        List<DayPart> dayPartsList = new ArrayList<DayPart>();
        StringTokenizer tokenizer = new StringTokenizer(dayPartsText, "&");
        while (tokenizer.hasMoreTokens()) {
            String dayPartText = tokenizer.nextToken().trim();
            dayPartsList.add(new DayPart(dayPartText, dateFormat, this, dayPartsList.size()));
        }
        dayParts = dayPartsList.toArray(new DayPart[dayPartsList.size()]);
    }

    public DayPart findNextDayPartStart(int dayPartIndex, Date date) {
        // if there is a day part in this day that starts after the given date
        if (dayPartIndex < dayParts.length) {
            if (dayParts[dayPartIndex].isStartAfter(date)) {
                return dayParts[dayPartIndex];
            } else {
                return findNextDayPartStart(dayPartIndex + 1, date);
            }
        } else {
            // descend recursively
            date = businessCalendar.findStartOfNextDay(date);
            Day nextDay = businessCalendar.findDay(date);
            return nextDay.findNextDayPartStart(0, date);
        }
    }

    public DayPart findPrevDayPartEnd(int dayPartIndex, Date date) {
        // if there is a day part in this day that ends before the given date
        if (dayPartIndex >= 0) {
            if (dayParts[dayPartIndex].isEndBefore(date)) {
                return dayParts[dayPartIndex];
            } else {
                return findPrevDayPartEnd(dayPartIndex - 1, date);
            }
        } else {
            // descend recursively
            date = businessCalendar.findEndOfPrevDay(date);
            Day prevDay = businessCalendar.findDay(date);
            return prevDay.findPrevDayPartEnd(prevDay.dayParts.length - 1, date);
        }
    }
}

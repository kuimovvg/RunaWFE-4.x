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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import ru.runa.wfe.InternalApplicationException;

/**
 * identifies a continuous set of days.
 */
public class Holiday implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date fromDay = null;
    private Date toDay = null;

    public static List<Holiday> parseHolidays(Properties calendarProperties) {
        List<Holiday> holidays = new ArrayList<Holiday>();
        DateFormat dateFormat = new SimpleDateFormat(calendarProperties.getProperty("day.format"));
        for (Object key : calendarProperties.keySet()) {
            if (key.toString().startsWith("holiday")) {
                Holiday holiday = new Holiday(calendarProperties.getProperty(key.toString()), dateFormat);
                holidays.add(holiday);
            }
        }
        return holidays;
    }

    public Holiday(String holidayText, DateFormat dateFormat) {
        try {
            int separatorIndex = holidayText.indexOf('-');
            if (separatorIndex == -1) {
                fromDay = dateFormat.parse(holidayText.trim());
                toDay = fromDay;
            } else {
                String fromText = holidayText.substring(0, separatorIndex).trim();
                String toText = holidayText.substring(separatorIndex + 1).trim();
                fromDay = dateFormat.parse(fromText);
                toDay = dateFormat.parse(toText);
            }
            // now we are going to set the toDay to the end of the day, rather then the beginning.
            // we take the start of the next day as the end of the toDay.
            Calendar calendar = BusinessCalendarImpl.getCalendar();
            calendar.setTime(toDay);
            calendar.add(Calendar.DATE, 1);
            toDay = calendar.getTime();
        } catch (ParseException e) {
            throw new InternalApplicationException("couldn't parse holiday '" + holidayText + "'", e);
        }
    }

    public boolean includes(Date date) {
        return fromDay.getTime() <= date.getTime() && date.getTime() < toDay.getTime();
    }
}

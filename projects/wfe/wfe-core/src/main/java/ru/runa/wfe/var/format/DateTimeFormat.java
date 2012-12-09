/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.CalendarUtil;

/**
 * Created on 30.11.2004
 * 
 */
public class DateTimeFormat extends AbstractDateFormat {

    public DateTimeFormat() {
        super(CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
    }

    // @Override
    // public Date parse(String[] source) throws ParseException {
    // if (source.length == 2) {
    // Date date = CalendarUtil.convertToDate(source[0],
    // CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
    // Date time = CalendarUtil.convertToDate(source[0],
    // CalendarUtil.HOURS_MINUTES_FORMAT);
    // Calendar calendar = CalendarUtil.dateToCalendar(date);
    // CalendarUtil.setTimeFromCalendar(calendar,
    // CalendarUtil.dateToCalendar(time));
    // return calendar.getTime();
    // }
    // return super.parse(source);
    // }

}

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

import java.text.ParseException;
import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;

public abstract class AbstractDateFormat implements VariableFormat {
    private final java.text.DateFormat dateTimeFormat;

    public AbstractDateFormat(java.text.DateFormat dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    @Override
    public Class<Date> getJavaClass() {
        return Date.class;
    }

    @Override
    public String format(Object object) {
        return CalendarUtil.format((Date) object, dateTimeFormat);
    }

    @Override
    public Date parse(String source) throws ParseException {
        if (source != null) {
            return CalendarUtil.convertToDate(source, dateTimeFormat);
        }
        return null;
    }
}
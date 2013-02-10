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
package ru.runa.common.web.html;

import java.util.Date;

import org.apache.ecs.html.TD;

import ru.runa.wfe.commons.CalendarUtil;

/**
 * Created on 14.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public abstract class BaseDateTDBuilder<T extends Object> implements TDBuilder {

    @Override
    public TD build(Object object, Env env) {
        String dateString;
        Date date = getDate((T) object);
        if (date != null) {
            dateString = CalendarUtil.formatDate(date);
        } else {
            dateString = "";
        }
        TD td = new TD(dateString);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        Date date = getDate((T) object);
        if (date != null) {
            return CalendarUtil.formatDateTime(date);
        }
        return "";
    }

    protected abstract Date getDate(T object);

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}

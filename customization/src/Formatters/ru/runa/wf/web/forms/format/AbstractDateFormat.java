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
package ru.runa.wf.web.forms.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.runa.commons.format.WebFormat;

public abstract class AbstractDateFormat implements WebFormat {
    private final SimpleDateFormat dateFormat;

    public AbstractDateFormat(String pattern) {
        dateFormat = new SimpleDateFormat(pattern);
    }

    @Override
    public String format(Object object) {
        return dateFormat.format((Date) object);
    }

    @Override
    public Object parse(String[] source) throws ParseException {
        return dateFormat.parseObject(source[0]);
    }
}

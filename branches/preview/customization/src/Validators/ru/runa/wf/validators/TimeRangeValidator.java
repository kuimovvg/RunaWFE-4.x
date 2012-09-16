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
package ru.runa.wf.validators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.runa.commons.validation.ValidationException;
import ru.runa.validators.AbstractRangeValidator;


public class TimeRangeValidator extends AbstractRangeValidator<Date> {
    private static final DateFormat FORMAT = new SimpleDateFormat("H:mm");

    public Date getParam(String name) throws ValidationException {
        Object obj = getParameter(name);
        if (obj == null) {
            return null;
        }
        if (obj instanceof Date) {
            return (Date) obj;
        }
        try {
            return FORMAT.parse((String) obj);
        } catch (ParseException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    protected Date getMaxComparatorValue() throws ValidationException {
        return getParam("max");
    }

    @Override
    protected Date getMinComparatorValue() throws ValidationException {
        return getParam("min");
    }
    
}

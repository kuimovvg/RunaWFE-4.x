/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.runa.commons.validation.ValidationException;


public class DateRangeFieldValidator extends AbstractRangeValidator<Date> {
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");

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

/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.wfe.validation.impl;

import java.util.Date;

import ru.runa.wfe.commons.TypeConversionUtil;

public class DateRangeFieldValidator extends AbstractRangeValidator<Date> {

    public Date getParam(String name) {
        return TypeConversionUtil.convertTo(getParameter(name), Date.class);
    }

    @Override
    protected Date getMaxComparatorValue() {
        return getParam("max");
    }

    @Override
    protected Date getMinComparatorValue() {
        return getParam("min");
    }
}

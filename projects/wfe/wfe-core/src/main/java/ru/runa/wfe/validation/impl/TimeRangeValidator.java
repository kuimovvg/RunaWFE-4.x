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
package ru.runa.wfe.validation.impl;

import java.util.Date;

import ru.runa.wfe.commons.TypeConversionUtil;

public class TimeRangeValidator extends AbstractRangeValidator<Date> {

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

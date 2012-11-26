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

/**
 * Base class for range based validators.
 */
public abstract class AbstractRangeValidator<T extends Object> extends FieldValidatorSupport {

    @Override
    public void validate() {
        Comparable<T> value = (Comparable<T>) getFieldValue();
        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the
        // field
        if (value == null) {
            return;
        }

        if (getMinComparatorValue() != null) {
            if (isInclusive()) {
                if (value.compareTo(getMinComparatorValue()) < 0) {
                    addFieldError();
                }
            } else {
                if (value.compareTo(getMinComparatorValue()) <= 0) {
                    addFieldError();
                }
            }
        }

        if (getMaxComparatorValue() != null) {
            if (isInclusive()) {
                if (value.compareTo(getMaxComparatorValue()) > 0) {
                    addFieldError();
                }
            } else {
                if (value.compareTo(getMaxComparatorValue()) >= 0) {
                    addFieldError();
                }
            }
        }
    }

    protected boolean isInclusive() {
        return true;
    }

    protected abstract T getMinComparatorValue();

    protected abstract T getMaxComparatorValue();

}

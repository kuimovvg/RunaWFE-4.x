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

import ru.runa.commons.validation.ValidationException;
import ru.runa.validators.FieldValidatorSupport;


public class ArraySizeValidator extends FieldValidatorSupport {
    private int minLength = -1;
    private int maxLength = -1;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public void validate() throws ValidationException {
        String[] var = (String[]) getFieldValue();
        if (var == null) {
            // use a required validator for these
            return;
        }

        int arraySize = var.length;

        if ((minLength > -1) && (arraySize < minLength)) {
            addFieldError();
        } else if ((maxLength > -1) && (arraySize > maxLength)) {
            addFieldError();
        }
    }
}

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
package ru.runa.validators;


import ru.runa.commons.validation.ValidationException;

public class StringLengthFieldValidator extends FieldValidatorSupport {

    private boolean doTrim = true;
    private int maxLength = -1;
    private int minLength = -1;


    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setTrim(boolean trim) {
        doTrim = trim;
    }

    public boolean getTrim() {
        return doTrim;
    }

    @Override
    public void validate() throws ValidationException {
        String val = (String) getFieldValue();

        if (val == null) {
            // use a required validator for these
            return;
        }
        if (doTrim) {
            val = val.trim();
            if (val.length() == 0) {
            	return;
            }
        }

        if ((minLength > -1) && (val.length() < minLength)) {
            addFieldError();
        } else if ((maxLength > -1) && (val.length() > maxLength)) {
            addFieldError();
        }
    }
}

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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import ru.runa.wfe.validation.FieldValidator;

public class ContainerElementsRequiredValidator extends FieldValidator {

    @Override
    public void validate() {
        Object container = getFieldValue();
        if (container == null) {
            // use a required validator for these
            return;
        }
        if (container instanceof Collection) {
            int index = 0;
            for (Object object : (Collection<?>) container) {
                if (isNullValue(object)) {
                    getValidatorContext().addFieldError(getFieldName() + "[" + index + "]", getMessage());
                }
                index++;
            }
        } else if (container.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(container); i++) {
                if (isNullValue(Array.get(container, i))) {
                    getValidatorContext().addFieldError(getFieldName() + "[" + i + "]", getMessage());
                }
            }
        } else if (container instanceof Map<?, ?>) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) container).entrySet()) {
                if (isNullValue(entry.getKey()) || isNullValue(entry.getValue())) {
                    addError();
                    return;
                }
            }
        } else {
            addError("Unexpected variable type: " + container.getClass());
            return;
        }
    }

    private boolean isNullValue(Object object) {
        return object == null || (object instanceof String && ((String) object).isEmpty());
    }
}

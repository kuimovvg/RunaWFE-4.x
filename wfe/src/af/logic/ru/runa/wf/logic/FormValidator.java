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
package ru.runa.wf.logic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import ru.runa.commons.validation.ValidationException;
import ru.runa.commons.validation.ValidatorContext;
import ru.runa.commons.validation.ValidatorManager;
import ru.runa.wf.form.VariablesValidationException;

public class FormValidator {

    public static void validate(String key, byte[] validationFile, Map<String, ? extends Object> variables) throws VariablesValidationException {
        try {
            InputStream is = new ByteArrayInputStream(validationFile);
            ValidatorContext context = ValidatorManager.getInstance().validate(key, is, variables);
            if (context.hasActionErrors() || context.hasFieldErrors()) {
                throw new VariablesValidationException(context.getFieldErrors(), context.getActionErrors());
            }
        } catch (ValidationException e) {
            throw new VariablesValidationException(e);
        }

    }
}

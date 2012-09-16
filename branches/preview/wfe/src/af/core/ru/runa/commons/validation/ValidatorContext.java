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
package ru.runa.commons.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidatorContext {
    private Collection<String> actionErrors = new ArrayList<String>();
    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();
    private final Map<String, ? extends Object> variables;

    // TODO add persistent variables check
    public ValidatorContext(Map<String, ? extends Object> variables) {
        this.variables = variables;
    }

    public Object getVariable(String varName) {
        return variables.get(varName);
    }

    public Map<String, ? extends Object> getVariables() {
        return variables;
    }

    public Collection<String> getActionErrors() {
        return new ArrayList<String>(actionErrors);
    }

    public Map<String, List<String>> getFieldErrors() {
        return new HashMap<String, List<String>>(fieldErrors);
    }

    public void addActionError(String anErrorMessage) {
        actionErrors.add(anErrorMessage);
    }

    public void addFieldError(String fieldName, String errorMessage) {
        List<String> thisFieldErrors = (List<String>) fieldErrors.get(fieldName);
        if (thisFieldErrors == null) {
            thisFieldErrors = new ArrayList<String>();
            fieldErrors.put(fieldName, thisFieldErrors);
        }
        thisFieldErrors.add(errorMessage);
    }

    public boolean hasActionErrors() {
        return !actionErrors.isEmpty();
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

}

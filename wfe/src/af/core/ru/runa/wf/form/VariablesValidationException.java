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
package ru.runa.wf.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

public class VariablesValidationException extends Exception {
    private static final long serialVersionUID = 5L;

    @XmlTransient
    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();
    private Collection<String> globalErrors = new ArrayList<String>();

    public VariablesValidationException() {
    }

    public VariablesValidationException(Exception e) {
        super(e);
    }

    public VariablesValidationException(Map<String, List<String>> fieldErrors, Collection<String> globalErrors) {
        if (fieldErrors != null) {
            this.fieldErrors = fieldErrors;
        }
        if (globalErrors != null) {
            this.globalErrors = globalErrors;
        }
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public Map<String, String> getConcatenatedFieldErrors() {
        if (fieldErrors == null) {
            return null;
        }
        Map<String, String> concatenated = new HashMap<String, String>();
        for (String key : fieldErrors.keySet()) {
            List<String> values = fieldErrors.get(key);
            String concat = null;
            if (values != null) {
                StringBuffer buffer = new StringBuffer();
                for (String msg : values) {
                    buffer.append(msg);
                    buffer.append("\n");
                }
                concat = buffer.toString();
            }
            concatenated.put(key, concat);
        }
        return concatenated;
    }

    public Collection<String> getGlobalErrors() {
        return globalErrors;
    }
}

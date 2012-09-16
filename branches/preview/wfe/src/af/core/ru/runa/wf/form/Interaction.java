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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Contains data for user interaction with process execution.
 */
public class Interaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String type;
    private final String stateName;
    private final byte[] formData;
    private final byte[] validationData;
    private final boolean useJSValidation;
    private final byte[] scriptData;
    private final List<String> requiredVariableNames = Lists.newArrayList();
    private final Map<String, VariableDefinition> variableDefinitions = Maps.newHashMap();
    private final Map<String, Object> defaultVariableValues = Maps.newHashMap();

    public Interaction(String type, String stateName, byte[] formData, byte[] validationData, boolean useJSValidation, byte[] scriptData) {
        this.type = type;
        this.stateName = stateName;
        this.formData = formData;
        this.validationData = validationData;
        this.useJSValidation = useJSValidation;
        this.scriptData = scriptData;
    }

    public byte[] getFormData() {
        return formData;
    }
    
    public byte[] getFormDataNotNull() {
        if (formData == null) {
            return ("No form defined for state: " + stateName).getBytes();
        }
        return formData;
    }

    public boolean hasFile() {
        return formData != null;
    }

    public String getType() {
        return type;
    }

    public boolean isUseJSValidation() {
        return useJSValidation && validationData != null;
    }

    public byte[] getValidationData() {
        return validationData;
    }

    public byte[] getScriptData() {
        return scriptData;
    }

    public List<String> getRequiredVariableNames() {
        return requiredVariableNames;
    }

    public String getStateName() {
        return stateName;
    }
    
    public Map<String, VariableDefinition> getVariables() {
        return variableDefinitions;
    }
    
    public Map<String, Object> getDefaultVariableValues() {
        return defaultVariableValues;
    }

}

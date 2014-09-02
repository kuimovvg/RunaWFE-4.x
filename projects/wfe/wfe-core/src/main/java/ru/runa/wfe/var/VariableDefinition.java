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
package ru.runa.wfe.var;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
public class VariableDefinition implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String FORMAT_COMPONENT_TYPE_START = "(";
    public static final String FORMAT_COMPONENT_TYPE_END = ")";
    public static final String FORMAT_COMPONENT_TYPE_CONCAT = ", ";

    private boolean synthetic;
    private String name;
    private String description;
    private String format;
    private boolean publicAccess;
    private Object defaultValue;
    private String scriptingName;
    private String formatLabel;
    private transient VariableFormat variableFormat;
    // TODO find more convenient way to reference user types
    private Map<String, VariableUserType> userTypes;

    public VariableDefinition() {
    }

    public VariableDefinition(boolean synthetic, String name, String scriptingName) {
        this.synthetic = synthetic;
        this.name = name;
        this.scriptingName = scriptingName;
    }

    public VariableDefinition(boolean synthetic, String name, String scriptingName, VariableFormat variableFormat) {
        this(synthetic, name, scriptingName);
        setFormat(variableFormat.toString());
        this.variableFormat = variableFormat;
    }

    public VariableDefinition(boolean synthetic, String name, String scriptingName, String format) {
        this(synthetic, name, scriptingName);
        setFormat(format);
    }

    public VariableDefinition(String name, String scriptingName, VariableDefinition attributeDefinition) {
        this(true, name, scriptingName, attributeDefinition.getFormat());
        setUserTypes(attributeDefinition.getUserTypes());
        setDefaultValue(attributeDefinition.getDefaultValue());
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScriptingName() {
        return scriptingName;
    }

    public String getScriptingNameWithoutDots() {
        return scriptingName.replaceAll("\\.", "_");
    }

    public VariableFormat getFormatNotNull() {
        if (variableFormat == null) {
            variableFormat = FormatCommons.create(this);
        }
        return variableFormat;
    }

    public String getFormatClassName() {
        if (format != null && format.contains(FORMAT_COMPONENT_TYPE_START)) {
            int index = format.indexOf(FORMAT_COMPONENT_TYPE_START);
            return format.substring(0, index);
        }
        return format;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String[] getFormatComponentClassNames() {
        if (format != null && format.contains(FORMAT_COMPONENT_TYPE_START)) {
            int index = format.indexOf(FORMAT_COMPONENT_TYPE_START);
            String raw = format.substring(index + 1, format.length() - 1);
            return raw.split(FORMAT_COMPONENT_TYPE_CONCAT, -1);
        }
        return new String[0];
    }

    public Map<String, VariableUserType> getUserTypes() {
        return userTypes;
    }

    public void setUserTypes(Map<String, VariableUserType> userTypes) {
        this.userTypes = userTypes;
    }

    public boolean isPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFormatLabel() {
        if (formatLabel != null) {
            return formatLabel;
        }
        if (getUserType() != null) {
            return getUserType().getName();
        }
        return format;
    }

    public void setFormatLabel(String formatLabel) {
        this.formatLabel = formatLabel;
    }

    public boolean isComplex() {
        return getUserType() != null;
    }

    public VariableUserType getUserType() {
        return userTypes != null ? userTypes.get(format) : null;
    }

    public List<VariableDefinition> expandComplexVariable(boolean preserveComplex) {
        return expandComplexVariable(this, this, preserveComplex);
    }

    private List<VariableDefinition> expandComplexVariable(VariableDefinition superVariable, VariableDefinition complexVariable,
            boolean preserveComplex) {
        List<VariableDefinition> result = Lists.newArrayList();
        for (VariableDefinition attributeDefinition : complexVariable.getUserType().getAttributes()) {
            String name = superVariable.getName() + VariableUserType.DELIM + attributeDefinition.getName();
            String scriptingName = superVariable.getScriptingName() + VariableUserType.DELIM + attributeDefinition.getScriptingName();
            VariableDefinition variable = new VariableDefinition(name, scriptingName, attributeDefinition);
            if (variable.isComplex()) {
                if (preserveComplex) {
                    result.add(variable);
                }
                result.addAll(expandComplexVariable(variable, attributeDefinition, preserveComplex));
            } else {
                result.add(variable);
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableDefinition) {
            VariableDefinition d = (VariableDefinition) obj;
            return Objects.equal(name, d.name);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName()).add("format", format).toString();
    }

}

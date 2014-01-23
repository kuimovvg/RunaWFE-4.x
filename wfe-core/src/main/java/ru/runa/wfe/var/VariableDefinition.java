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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
public class VariableDefinition implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String FORMAT_COMPONENT_TYPE_START = "(";
    public static final String FORMAT_COMPONENT_TYPE_END = ")";
    public static final String FORMAT_COMPONENT_TYPE_CONCAT = ", ";

    private boolean syntetic;
    private String name;
    private String format;
    private boolean publicAccess;
    private String defaultValue;
    private String scriptingName;
    private String formatLabel;
    private VariableUserType userType;

    public VariableDefinition() {
    }

    public VariableDefinition(boolean syntetic, String name, String scriptingName) {
        this.syntetic = syntetic;
        this.name = name;
        this.scriptingName = scriptingName;
    }

    public VariableDefinition(boolean syntetic, String name, String scriptingName, String format) {
        this(syntetic, name, scriptingName);
        setFormat(format);
    }

    public VariableDefinition(String name, VariableDefinition attributeDefinition) {
        this(true, name, name, attributeDefinition.getFormat());
    }
    
    public boolean isSyntetic() {
        return syntetic;
    }

    public String getName() {
        return name;
    }

    public String getScriptingName() {
        return scriptingName;
    }

    public String getScriptingNameWithoutDots() {
        return scriptingName.replaceAll(".", "_");
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

    public boolean isPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFormatLabel() {
        if (formatLabel != null) {
            return formatLabel;
        }
        if (userType != null) {
            return userType.getName();
        }
        return format;
    }

    public void setFormatLabel(String formatLabel) {
        this.formatLabel = formatLabel;
    }
    
    public boolean isComplex() {
        return userType != null;
    }
    
    public VariableUserType getUserType() {
        return userType;
    }
    
    public void setUserType(VariableUserType userType) {
        this.userType = userType;
    }

    public List<VariableDefinition> expandComplexVariable() {
        return expandComplexVariable(this, this);
    }

    private List<VariableDefinition> expandComplexVariable(VariableDefinition superVariable, VariableDefinition complexVariable) {
        List<VariableDefinition> result = Lists.newArrayList();
        for (VariableDefinition attributeDefinition : complexVariable.getUserType().getAttributes()) {
            String name = superVariable.getName() + VariableUserType.DELIM + attributeDefinition.getName();
            String scriptingName = superVariable.getScriptingName() + VariableUserType.DELIM + attributeDefinition.getScriptingName();
            VariableDefinition variable = new VariableDefinition(true, name, scriptingName, attributeDefinition.getFormat());
            variable.setUserType(attributeDefinition.getUserType());
            if (variable.isComplex()) {
                result.addAll(expandComplexVariable(variable, attributeDefinition));
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

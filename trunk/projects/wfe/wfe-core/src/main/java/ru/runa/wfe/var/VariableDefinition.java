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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.base.Objects;

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

    public VariableDefinition() {
    }

    public VariableDefinition(boolean syntetic, String name, String format, String scriptingName) {
        this.syntetic = syntetic;
        this.name = name;
        this.format = format;
        this.scriptingName = scriptingName;
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

    public String getFormatClassName() {
        if (format!=null && format.contains(FORMAT_COMPONENT_TYPE_START)) {
            int index = format.indexOf(FORMAT_COMPONENT_TYPE_START);
            return format.substring(0, index);
        }
        return format;
    }

    public String[] getFormatComponentClassNames() {
        if (format!=null && format.contains(FORMAT_COMPONENT_TYPE_START)) {
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
        return format;
    }

    public void setFormatLabel(String formatLabel) {
        this.formatLabel = formatLabel;
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

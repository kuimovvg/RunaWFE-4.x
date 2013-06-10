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

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Objects;

public class VariableDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean syntetic;
    private String name;
    private String formatClassName;
    private boolean publicAccess;
    private String defaultValue;
    @XmlTransient
    private String scriptingName;
    @XmlTransient
    private String formatLabel;

    public VariableDefinition() {
    }

    public VariableDefinition(boolean syntetic, String name, String formatClassName, String scriptingName) {
        this.syntetic = syntetic;
        this.name = name;
        this.formatClassName = formatClassName;
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
        return formatClassName;
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
        return formatClassName;
    }

    public void setFormatLabel(String displayFormat) {
        formatLabel = displayFormat;
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
        return Objects.toStringHelper(this).add("format", formatClassName).add("name", getName()).toString();
    }

}

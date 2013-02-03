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

import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;

/**
 * Created on 17.11.2004
 */
public class VariableDefinition implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String formatClassName;
    private String formatLabel;
    private boolean publicAccess;
    private String defaultValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormatClassName() {
        return formatClassName;
    }

    public void setFormatClassName(String formatClassName) {
        this.formatClassName = formatClassName;
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
    public String toString() {
        return Objects.toStringHelper(this).add("format", formatClassName).add("name", getName()).toString();
    }

    public VariableFormat<Object> getFormat() {
        return FormatCommons.create(this);
    }
}

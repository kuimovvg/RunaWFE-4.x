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

/**
 * Created on 17.11.2004 TODO rename to VariableDefinition
 */
public class VariableDefinition implements Serializable {
    private static final long serialVersionUID = 155229769452019112L;
    private final String name;
    private final String format;
    private final boolean isPublic;
    private final String defaultValue;

    /**
     * Creates variable
     * 
     * @param name
     *            variable name
     * @param format
     *            variable formatter class name
     */
    public VariableDefinition(String name, String format) {
        this.name = name;
        this.format = format;
        this.isPublic = false;
        this.defaultValue = null;
    }

    /**
     * Creates variable
     * 
     * @param name
     *            variable name
     * @param format
     *            variable formatter class name
     */
    public VariableDefinition(String name, String format, boolean isPublic, String defaultValue) {
        this.name = name;
        this.format = format;
        this.isPublic = isPublic;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VariableDefinition)) {
            return false;
        }
        VariableDefinition variableDefinition = (VariableDefinition) obj;
        if (getName().equals(variableDefinition.getName()) && getFormat().equals(variableDefinition.getFormat())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + name.hashCode();
        result = 37 * result + format.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name + " (" + format + ")";
    }

}

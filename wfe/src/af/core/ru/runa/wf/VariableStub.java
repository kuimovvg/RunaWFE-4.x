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
package ru.runa.wf;

import java.io.Serializable;

import ru.runa.wf.form.VariableDefinition;

/**
 * Created on 12.04.2005 TODO used only in web. Really need here?
 */
public class VariableStub implements Serializable {

    private static final long serialVersionUID = -5617263528374994625L;

    private final VariableDefinition definition;

    private final Object value;

    public VariableStub(VariableDefinition variableDefinition, Object value) {
        this.definition = variableDefinition;
        this.value = value;
    }

    public VariableDefinition getDefinition() {
        return definition;
    }

    public Object getValue() {
        return value;
    }

    public String getClassName() {
        String className = value != null ? value.getClass().getName() : "";
        return className;
    }

}

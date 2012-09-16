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
package ru.runa.wf.web.html.vartag;

import ru.runa.wf.web.html.VarTag;

/**
 * Signals that {@link VarTag} doesn't support varriable type.
 * When this exception is thrown, message is shown instead of task form.
 * Created on 05.05.2005
 */

public class VarTagTypeMismatchException extends Exception {

    private static final long serialVersionUID = -3641598046414246023L;
    private final String variableName;
    private final Class variableClass;
    private final Class expectedClass;
    private final Class varTagClass;

    public VarTagTypeMismatchException(String variableName, Class varTagClass, Class variableClass, Class expectedClass) {
        super("VarTag " + varTagClass + " cannot handle variable " + variableName + " of type " + variableClass + ". Type " + expectedClass
                + " required.");
        this.variableName = variableName;
        this.varTagClass = varTagClass;
        this.variableClass = variableClass;
        this.expectedClass = expectedClass;
    }

    public Class getExpectedType() {
        return expectedClass;
    }

    public String getVariableName() {
        return variableName;
    }

    public Class getVariableType() {
        return variableClass;
    }

    public Class getVarTagClass() {
        return varTagClass;
    }
}

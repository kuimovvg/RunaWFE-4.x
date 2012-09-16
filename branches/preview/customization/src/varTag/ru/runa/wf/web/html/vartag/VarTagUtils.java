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

import ru.runa.wf.web.html.WorkflowFormProcessingException;

/**
 * Created on 09.05.2005
 * 
 */
public class VarTagUtils {

    /**
     * Prevents instantiation.
     */
    private VarTagUtils() {
    }

    /**
     * @param varName variable name
     * @param var variable
     * @throws WorkflowFormProcessingException in case var==null
     */
    public static void checkNotNull(String varName, Object var) throws WorkflowFormProcessingException {
        if (var == null) {
            throw new WorkflowFormProcessingException("Variable " + varName + " was not initialized.");
        }
    }

    public static WorkflowFormProcessingException createTypeMismatchException(String variableName, Object variableValue, Class varTagClass,
            Class expectedClass) {
        Class variableClass = (variableValue != null) ? variableValue.getClass() : null;
        VarTagTypeMismatchException varTagTypeMismatchException = new VarTagTypeMismatchException(variableName, varTagClass, variableClass,
                expectedClass);
        return new WorkflowFormProcessingException(varTagTypeMismatchException);
    }
}

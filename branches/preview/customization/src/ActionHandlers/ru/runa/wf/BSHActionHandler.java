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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.ConfigurationException;
import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import bsh.Interpreter;

public class BSHActionHandler implements ActionHandler {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(BSHActionHandler.class);
    private String configuration;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            Set<String> variablesToOutput = new HashSet<String>();
            for (String variableName : executionContext.getProcessDefinition().getVariables().keySet()) {
                variablesToOutput.add(variableName);
            }
            Map<String, Object> variables = executionContext.getContextInstance().getVariables();
            Interpreter interpreter = new Interpreter();
            for (String variableName : variables.keySet()) {
                Object variableValue = variables.get(variableName);
                interpreter.set(variableName, variableValue);
                variablesToOutput.add(variableName);
            }
            interpreter.eval(configuration);
            for (String variableName : variablesToOutput) {
                Object bshVariableValue = interpreter.get(variableName);
                if (differs(bshVariableValue, variables.get(variableName))) {
                    log.info("Setting var '" + variableName + "' to " + bshVariableValue);
                    executionContext.setVariable(variableName, bshVariableValue);
                }
            }
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    private boolean differs(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return false;
        }
        if (o1 == null || o2 == null) {
            return true;
        }
        return !o1.equals(o2);
    }
}

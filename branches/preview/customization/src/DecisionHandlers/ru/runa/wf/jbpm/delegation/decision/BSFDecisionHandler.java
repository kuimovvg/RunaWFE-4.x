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
package ru.runa.wf.jbpm.delegation.decision;

import java.util.Map;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.node.DecisionHandler;

public class BSFDecisionHandler implements DecisionHandler {
    private static final long serialVersionUID = 1L;

    private String configuration;

    private final BSFManager manager = new BSFManager();
    static {
        String[] extensions = { "bsh" };
        BSFManager.registerScriptingEngine("beanshell", "bsh.util.BeanShellBSFEngine", extensions);
    }

    public BSFDecisionHandler(String configuration) {
        this.configuration = configuration;
    }

    public BSFDecisionHandler() {
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String decide(ExecutionContext executionContext) {
        Map<String, Object> variables = executionContext.getContextInstance().getVariables();
        try {
            for (String variableName : variables.keySet()) {
                Object variableValue = variables.get(variableName);
                if (variableValue != null) {
                    manager.declareBean(variableName, variableValue, variableValue.getClass());
                }
            }
            return (String) manager.eval("beanshell", null, 1, 1, configuration);
        } catch (BSFException e) {
            throw new InternalApplicationException(e);
        }
    }
}

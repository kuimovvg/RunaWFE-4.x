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
package ru.runa.wfe.var.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableUserType;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Process execution logic.
 * 
 * @author Dofs
 * @since 2.0
 */
public class VariableLogic extends WFCommonLogic {

    public List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException {
        List<WfVariable> result = Lists.newArrayList();
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        Map<String, Object> values = variableDAO.getAll(process);
        for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
            Object value;
            if (variableDefinition.isComplex()) {
                value = buildComplexVariable(variableDefinition, values, variableDefinition.getName());
            } else {
                value = values.remove(variableDefinition.getName());
            }
            if (value != null) {
                result.add(new WfVariable(variableDefinition, value));
            }
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.add(new WfVariable(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private ComplexVariable buildComplexVariable(VariableDefinition variableDefinition, Map<String, Object> values, String prefix) {
        ComplexVariable variable = new ComplexVariable();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            String variableName = prefix + VariableUserType.DELIM + attributeDefinition.getName();
            if (attributeDefinition.isComplex()) {
                ComplexVariable complexVariable = buildComplexVariable(attributeDefinition, values, variableName);
                if (complexVariable != null) {
                    variable.put(attributeDefinition.getName(), complexVariable);
                }
            } else {
                Object value = values.remove(variableName);
                if (value != null) {
                    variable.put(attributeDefinition.getName(), value);
                }
            }
        }
        // if (variable.isEmpty()) {
        // return null;
        // }
        return variable;
    }

    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        return executionContext.getVariableProvider().getVariable(variableName);
    }

    public HashMap<Long, WfVariable> getVariablesFromProcesses(User user, List<Long> processIds, String variableName) {
        // TODO potential performance bottleneck
        HashMap<Long, WfVariable> map = Maps.newHashMapWithExpectedSize(processIds.size());
        for (Long processId : processIds) {
            try {
                WfVariable variable = getVariable(user, processId, variableName);
                if (variable != null) {
                    map.put(processId, variable);
                }
            } catch (Exception e) {
                log.error("Unable to get variable '" + variableName + "' from process " + processId, e);
            }
        }
        return map;
    }

    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        Process process = processDAO.getNotNull(processId);
        // TODO check ProcessPermission.UPDATE
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        executionContext.setVariableValues(variables);
    }

}

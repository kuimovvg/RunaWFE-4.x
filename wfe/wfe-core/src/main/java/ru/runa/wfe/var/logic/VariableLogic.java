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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;

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
        Map<String, Object> variables = variableDAO.getAll(process);
        for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
            Object value = variables.get(variableDefinition.getName());
            result.add(new WfVariable(variableDefinition, value));
        }
        return result;
    }

    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        if (!processDefinition.isVariablePublic(variableName)) {
            // TODO checkReadToVariablesAllowed(user, task);
        }
        VariableDefinition variableDefinition = processDefinition.getVariable(variableName);
        if (variableDefinition == null) {
            SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlane(variableName);
            if (swimlaneDefinition != null) {
                variableDefinition = swimlaneDefinition.toVariableDefinition();
            } else {
                variableDefinition = new VariableDefinition();
                variableDefinition.setName(variableName);
            }
        }
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        Object variableValue = executionContext.getVariable(variableName);
        return new WfVariable(variableDefinition, variableValue);
    }

    public Map<Long, Object> getVariableValueFromProcesses(User user, List<Long> processIds, String variableName) {
        List<Identifiable> idents = Lists.newArrayListWithExpectedSize(processIds.size());
        for (Long processId : processIds) {
            Process stub = new Process();
            stub.setId(processId);
            idents.add(stub);
        }
        idents = filterIdentifiable(user, idents, ProcessPermission.READ);
        List<Long> readableProcesses = new ArrayList<Long>();
        for (Identifiable identifiable : idents) {
            readableProcesses.add(identifiable.getIdentifiableId());
        }
        return processDAO.getVariableValueFromProcesses(processIds, variableName);
    }

    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        Process process = processDAO.getNotNull(processId);
        checkPermissionAllowed(user, process, ProcessPermission.UPDATE_PERMISSIONS);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        executionContext.setVariables(variables);
    }

}

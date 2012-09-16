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
package ru.runa.wf.jbpm.delegation.assignment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.ConfigurationException;
import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.af.organizationfunction.OrgFunctionHelper;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.taskmgmt.exe.Assignable;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.commons.InfoHolder;
import ru.runa.wf.dao.TmpDAO;
import ru.runa.wf.logic.JbpmCommonLogic;

/**
 * Created on 09.12.2004
 * 
 */
public class AssignmentHandler implements ru.runa.bpm.taskmgmt.def.AssignmentHandler {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(AssignmentHandler.class);

    // Function to calculate assigned actor
    private String swimlaneInititalizer = null;

    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    protected TmpDAO tmpDAO;
    @Autowired
    protected ExecutorDAO executorDAO;

    public AssignmentHandler(String configuration) throws ConfigurationException {
        setConfiguration(configuration);
    }

    public AssignmentHandler() throws ConfigurationException {
    }

    public void setConfiguration(String configuration) {
        // Parse and save assignment parameters separate each other
        int functionSeparatorIndex = configuration.indexOf(JbpmCommonLogic.FUNCTION_SEPARATER);
        swimlaneInititalizer = (functionSeparatorIndex == -1) ? configuration : configuration.substring(0, functionSeparatorIndex);
    }

    @Override
    public void assign(Assignable assignable, ExecutionContext executionContext) throws Exception {
        try {
            // Get executor's ID, which will be assigned.
            List<Long> orgFunctionIds = OrgFunctionHelper.evaluateOrgFunction(executionContext.getTaskInstance(), swimlaneInititalizer, null);

            // Create (or get if exist) group G, which contains all executors,
            // asigned to task.
            // Substitutors also in group G. Assign task to what group G.
            // We assume, what assigned executors set is immutable during token
            // livetime. If this
            // condition violated, need to reassign token.
            // (TODO) Reassign can be done as:
            // 1. Periodically reassign all tasks (at night, then loading is
            // small)
            // 2. On state one of executor's in set is changing (new Actor in
            // group, and so one)
            if (orgFunctionIds == null || orgFunctionIds.size() == 0) {
                // Nobody can be assigned. Return unassigned value.
                assignable.setActorId(executionContext, InfoHolder.UNASSIGNED_SWIMLANE_VALUE);
                return;
            }

            Set<Executor> executors = new HashSet<Executor>();
            // Find all executors suitable to assign
            for (Long executorId : orgFunctionIds) {
                Executor curExecutor = executorDAO.getExecutor(executorId);
                // Current executor is sutable (returned by orgFunction)
                executors.add(curExecutor);
            }

            if (executors.size() == 1) {
                Executor aloneExecutor = (executors.iterator().next());
                if (aloneExecutor instanceof Actor) {
                    assignable.setActorId(executionContext, String.valueOf(((Actor) aloneExecutor).getCode()));
                } else {
                    assignable.setActorId(executionContext, "G" + String.valueOf(aloneExecutor.getId()));
                }
                return;
            }

            Group tokenGroup = executorLogic.getTemporaryGroup(executionContext.getProcessInstance().getId(), executionContext.getTask()
                    .getSwimlane().getName(), executors);
            executionContext.getToken().addLog(
                    new TaskAssignLog(executionContext.getTaskInstance(), Group.TEMPORARY_GROUP_PREFIX + tokenGroup.getId(), executorLogic
                            .encodeExecutors(executors)));
            tmpDAO.saveProcessInstance(executionContext.getToken().getProcessInstance()); // executionContext.getJbpmContext().addAutoSaveToken(executionContext.getToken());
            assignable.setActorId(executionContext, "G" + String.valueOf(tokenGroup.getId()));

            return;
        } catch (Exception e) {
            log.warn("Unable to assign in process id = " + executionContext.getProcessInstance().getId(), e);
        }
        assignable.setActorId(executionContext, InfoHolder.UNASSIGNED_SWIMLANE_VALUE);
    }
}

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
package ru.runa.wfe.job.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;

/**
 * Try to assign unassigned tasks.
 * 
 * @author Konstantinov Aleksey
 */
public class TaskAssigner extends TransactionalExecutor {
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private TaskDAO taskDAO;

    @Override
    protected void doExecuteInTransaction() {
        List<Task> unassignedTasks = taskDAO.findUnassignedTasks();
        log.debug("Unassigned tasks: " + unassignedTasks.size());
        for (Task unassignedTask : unassignedTasks) {
            execute(unassignedTask);
        }
    }

    private void execute(Task unassignedTask) {
        if (unassignedTask.getProcess().hasEnded()) {
            log.error("Deleting task for finished process: " + unassignedTask);
            unassignedTask.delete();
            return;
        }
        try {
            ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(unassignedTask.getProcess());
            if (unassignedTask.getSwimlane() != null) {
                Delegation delegation = processDefinition.getSwimlaneNotNull(unassignedTask.getSwimlane().getName()).getDelegation();
                AssignmentHandler handler = delegation.getInstance();
                handler.assign(new ExecutionContext(processDefinition, unassignedTask), unassignedTask);
            }
            ProcessExecutionErrors.removeProcessError(unassignedTask.getProcess().getId(), unassignedTask.getNodeId());
        } catch (Throwable th) {
            log.warn(
                    "Unable to assign task '" + unassignedTask + "' in " + unassignedTask.getProcess() + " with swimlane '"
                            + unassignedTask.getSwimlane() + "'", th);
            ProcessExecutionException e = new ProcessExecutionException(ProcessExecutionException.TASK_ASSIGNMENT_FAILED, th,
                    unassignedTask.getName());
            ProcessExecutionErrors.addProcessError(unassignedTask, e);
        }
    }

}

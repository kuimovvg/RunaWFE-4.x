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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
public class TaskAssigner {
    private static final Log log = LogFactory.getLog(TaskAssigner.class);

    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private TaskDAO taskDAO;

    public boolean areUnassignedTasksExist() {
        return taskDAO.findUnassignedTasks().size() > 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute() {
        List<Task> unassignedTasks = taskDAO.findUnassignedTasks();
        for (Task task : unassignedTasks) {
            if (task.getProcess().hasEnded()) {
                log.error("Deleting task for finished process: " + task);
                task.delete();
                continue;
            }
            try {
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(task);
                if (task.getSwimlane() != null) {
                    Delegation delegation = processDefinition.getSwimlaneNotNull(task.getSwimlane().getName()).getDelegation();
                    AssignmentHandler handler = delegation.getInstance();
                    handler.assign(new ExecutionContext(processDefinition, task), task);
                }
                ProcessExecutionErrors.removeProcessError(task.getProcess().getId(), task.getName());
            } catch (Throwable th) {
                log.warn("Unable to assign task '" + task + "' with swimlane '" + task.getSwimlane() + "'", th);
                ProcessExecutionException e = new ProcessExecutionException(ProcessExecutionException.TASK_ASSIGNMENT_FAILED, th, task.getName());
                ProcessExecutionErrors.addProcessError(task.getProcess().getId(), task.getName(), e);
            }
        }

    }

}

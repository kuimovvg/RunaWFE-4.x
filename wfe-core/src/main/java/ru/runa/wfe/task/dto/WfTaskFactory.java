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

package ru.runa.wfe.task.dto;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Objects;

/**
 * {@link WfTask} factory.
 * 
 * @author Dofs
 * @since 4.0
 */
public class WfTaskFactory {
    private static final Log log = LogFactory.getLog(WfTaskFactory.class);
    @Autowired
    private ExecutorDAO executorDAO;
    private double taskAlmostDeadline;

    @Required
    public void setTaskAlmostDeadline(double taskAlmostDeadline) {
        this.taskAlmostDeadline = taskAlmostDeadline;
    }

    public WfTask create(Task task, Actor targetActor, String formType) {
        Process process = task.getProcess();
        Deployment deployment = process.getDeployment();
        boolean groupAssigned = task.getExecutor() instanceof Group;
        boolean escalated = false;
        try {
            if (groupAssigned) {
                Group group = (Group) task.getExecutor();
                if (group instanceof EscalationGroup) {
                    EscalationGroup escalationGroup = (EscalationGroup) group;
                    Executor originalExecutor = escalationGroup.getOriginalExecutor();
                    if (originalExecutor instanceof Group) {
                        escalated = !executorDAO.isExecutorInGroup(targetActor, (Group) originalExecutor);
                    } else {
                        escalated = !Objects.equal(originalExecutor, targetActor);
                    }
                }
            }
        } catch (Exception e) {
            log.error("escalation", e);
        }
        return new WfTask(task, process.getId(), deployment, getDeadlineWarningDate(task), groupAssigned, escalated);
    }

    public Date getDeadlineWarningDate(Task task) {
        if (task.getCreateDate() == null || task.getDeadlineDate() == null) {
            return null;
        }
        long duration = task.getDeadlineDate().getTime() - task.getCreateDate().getTime();
        return new Date(task.getDeadlineDate().getTime() - (long) (duration * taskAlmostDeadline));
    }

}

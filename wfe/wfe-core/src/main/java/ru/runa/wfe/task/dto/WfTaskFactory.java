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

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.SystemProperties;
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
    @Autowired
    private ExecutorDAO executorDAO;

    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution) {
        Process process = task.getProcess();
        Deployment deployment = process.getDeployment();
        boolean escalated = false;
        if (task.getExecutor() instanceof Group) {
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
        return new WfTask(task, deployment, process.getId(), targetActor, getDeadlineWarningDate(task), escalated, acquiredBySubstitution);
    }

    public Date getDeadlineWarningDate(Task task) {
        return getDeadlineWarningDate(task.getCreateDate(), task.getDeadlineDate());
    }

    public Date getDeadlineWarningDate(Date createDate, Date deadlineDate) {
        if (createDate == null || deadlineDate == null) {
            return null;
        }
        int percents = SystemProperties.getTaskAlmostDeadlineInPercents();
        long duration = deadlineDate.getTime() - createDate.getTime();
        return new Date(createDate.getTime() + duration * percents / 100);
    }

}

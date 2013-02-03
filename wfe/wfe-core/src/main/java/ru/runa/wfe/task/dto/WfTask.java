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

import java.io.Serializable;
import java.util.Date;

import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Objects;

/**
 * Process task.
 * 
 * @author Dofs
 * @since 4.0
 */
public final class WfTask implements Serializable {
    private static final long serialVersionUID = 3415182898189086844L;

    private final Long id;
    private final String name;
    private final String description;
    private final String swimlaneName;
    private final Executor owner;
    private final Long definitionId;
    private final String definitionName;
    private final Long processId;

    private final Date creationDate;
    private final Date deadlineDate;
    private final Date deadlineWarningDate;
    private final boolean escalated;
    private final boolean groupAssigned;
    private final boolean firstOpen;

    public WfTask(Task task, Long processId, Deployment deployment, Date deadlineWarningDate, boolean groupAssigned, boolean escalated) {
        id = task.getId();
        name = task.getName();
        description = task.getDescription();
        owner = task.getExecutor();
        this.processId = processId;
        definitionId = deployment.getId();
        definitionName = deployment.getName();
        swimlaneName = task.getSwimlane() != null ? task.getSwimlane().getName() : "";
        creationDate = task.getCreateDate();
        deadlineDate = task.getDeadlineDate();
        this.deadlineWarningDate = deadlineWarningDate;
        this.groupAssigned = groupAssigned;
        this.escalated = escalated;
        firstOpen = task.isFirstOpen();
    }

    public boolean isFirstOpen() {
        return firstOpen;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSwimlaneName() {
        return swimlaneName;
    }

    public Executor getOwner() {
        return owner;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public Long getProcessId() {
        return processId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public Date getDeadlineWarningDate() {
        return deadlineWarningDate;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public boolean isGroupAssigned() {
        return groupAssigned;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("definitionId", definitionId).add("processId", processId).add("id", id).add("name", name).toString();
    }

}

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

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
@XmlAccessorType(XmlAccessType.FIELD)
public final class WfTask implements Serializable {
    private static final long serialVersionUID = 3415182898189086844L;

    private Long id;
    private String name;
    private String description;
    private String swimlaneName;
    private Executor owner;
    private Long definitionId;
    private String definitionName;
    private Long processId;

    private Date creationDate;
    private Date deadlineDate;
    private Date deadlineWarningDate;
    private boolean escalated;
    private boolean groupAssigned;
    private boolean firstOpen;

    public WfTask() {
    }

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
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfTask) {
            return Objects.equal(id, ((WfTask) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("definitionId", definitionId).add("processId", processId).add("id", id).add("name", name).toString();
    }

}

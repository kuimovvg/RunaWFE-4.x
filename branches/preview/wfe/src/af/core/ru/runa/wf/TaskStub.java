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

package ru.runa.wf;

import java.io.Serializable;
import java.util.Date;

import ru.runa.af.Actor;
import ru.runa.af.IdentifiableBaseImpl;
import ru.runa.commons.system.CommonResources;

/**
 * Created on 12.11.2004
 * 
 */
public final class TaskStub extends IdentifiableBaseImpl implements Serializable {
    private static final long serialVersionUID = 3415182898189086844L;

    private final Long id;

    private final String name;

    private final String description;

    private final Actor targetActor;
    
    private final boolean isEscalated;

	private final Long processInstanceId;

    private final Long processDefinitionId;
    private final String processDefinitionName;
    private final int processDefinitionVersion;

    private final Long superProcessInstanceId;
    private final String superProcessDefinitionName;
    private final Long superProcessDefinitionId;

    private final SwimlaneStub swimlaneStub;
    private final Date creationDate;

	private final Date deadline;

    private final String formType;

    private final boolean groupAssigned;

    private boolean firstOpen;

    public TaskStub(Long id, String name, String description, Actor targetActor, Long processInstanceId, Long processDefinitionId,
            String processDefinitionName, int processDefinitionVersion, Long superProcessInstanceId, String superProcessDefinitionName,
            Long superProcessDefinitionId, SwimlaneStub swimlaneStub, Date creationDate, Date deadline, String formType, boolean groupAssigned, boolean isEscalated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.targetActor = targetActor;
        this.processInstanceId = processInstanceId;
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionName = processDefinitionName;
        this.processDefinitionVersion = processDefinitionVersion;
        this.superProcessInstanceId = superProcessInstanceId;
        this.superProcessDefinitionName = superProcessDefinitionName;
        this.superProcessDefinitionId = superProcessDefinitionId;
        this.swimlaneStub = swimlaneStub;
        this.creationDate = creationDate;
        this.deadline = deadline;
        this.formType = formType;
        this.groupAssigned = groupAssigned;
        this.isEscalated = isEscalated; 
        firstOpen = false;
    }

    public boolean isEscalated() {
		return isEscalated;
	}
    
    public static Date calculateAlmostDeadlineDate(Date startDate, Date deadline) {
    	long duration = deadline.getTime() - startDate.getTime();
        String almostDeadlineProperty = new CommonResources().readPropertyIfExist("default.task.almostDeadline");
        if (almostDeadlineProperty == null) almostDeadlineProperty = "90%";
    	almostDeadlineProperty = almostDeadlineProperty.trim();
    	if (almostDeadlineProperty.endsWith("%")) almostDeadlineProperty = almostDeadlineProperty.substring(0, almostDeadlineProperty.length()-1);
    	double v = 1 - Integer.parseInt(almostDeadlineProperty) / 100.0;
        return new Date(deadline.getTime() - (long)(duration * v));
    }

    public Long getId() {
        return id;
    }
    
    public Date getCreationDate() {
		return creationDate;
	}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Actor getTargetActor() {
        return targetActor;
    }

    public String getTargetActorName() {
        return targetActor.getName();
    }

    public String getRole() {
        return swimlaneStub.getName();
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TaskStub)) {
            return false;
        }
        TaskStub taskStub = (TaskStub) obj;
        if (getId() == taskStub.getId() && getProcessInstanceId() == taskStub.getProcessInstanceId()
                && (getTargetActor() == null || (getTargetActor().equals(taskStub.getTargetActor())))) {
            return true;
        }
        return false;
    }

    public boolean equalsWeek(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TaskStub)) {
            return false;
        }
        TaskStub taskStub = (TaskStub) obj;
        if (getId() == taskStub.getId() && getProcessInstanceId() == taskStub.getProcessInstanceId()) {
            return true;
        }
        return false;
    }

    private int hashCode = 0;

    public int hashCode() {
        if (hashCode == 0) {
            hashCode = 17;
            hashCode = 37 * hashCode + (int) (getId() ^ (getId() >>> 32));
            hashCode = 37 * hashCode + (int) (getProcessInstanceId() ^ (getProcessInstanceId() >>> 32));
            if (getTargetActor() != null) {
                hashCode = 37 * hashCode + getTargetActor().hashCode();
            }
        }
        return hashCode;
    }

    public String toString() {
        return "Task Id: " + id + ", Task Name: " + getName() + ", Instance Id: " + getProcessInstanceId() + ", Definition Name: "
                + getProcessDefinitionName() + ", Definition Version: " + getProcessDefinitionVersion();
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public SwimlaneStub getSwimlaneStub() {
        return swimlaneStub;
    }

    /**
     * @return the groupAssigned
     */
    public boolean isGroupAssigned() {
        return groupAssigned;
    }

    public Date getDeadline() {
        return deadline;
    }

    public Long getSuperProcessId() {
        return superProcessInstanceId;
    }

    public String getSuperProcessDefinitionName() {
        return superProcessDefinitionName;
    }

    public Long getSuperProcessDefinitionId() {
        return superProcessDefinitionId;
    }

    public String getFormType() {
        return formType;
    }

    public boolean isFirstOpen() {
        return firstOpen;
    }

    public void setFirstOpen(boolean firstOpen) {
        this.firstOpen = firstOpen;
    }
}

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

import ru.runa.af.Executor;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;

public final class SwimlaneStub implements Serializable {

    private static final long serialVersionUID = -2295403897054740911L;

    private final Long id;
    private final Long processInstanceId;
    private final String name;
    private final boolean isAssigned;
    private final Executor executor;
    private final String orgFunction;

    private SwimlaneStub(Long processInstanceId) {
        id = Long.MIN_VALUE;
        this.processInstanceId = processInstanceId;
        name = "";
        isAssigned = false;
        executor = null;
        orgFunction = "";
    }

    public SwimlaneStub(TaskInstance taskInstance, Executor assignedExecutor) {
        if (taskInstance.getSwimlaneInstance() == null) {
            id = Long.MIN_VALUE;
            processInstanceId = taskInstance.getProcessInstance().getId();
            name = "";
            isAssigned = false;
            executor = null;
            orgFunction = "";
            return;
        }
        Swimlane swimlane = taskInstance.getSwimlaneInstance().getSwimlane();
        String configuration = swimlane.getDelegation() == null ? "" : swimlane.getDelegation().getConfiguration();
        id = swimlane.getId();
        processInstanceId = taskInstance.getProcessInstance().getId();
        name = swimlane.getName();
        isAssigned = assignedExecutor != null;
        executor = assignedExecutor;
        orgFunction = configuration;
    }

    public SwimlaneStub(Swimlane swimlane, Long processInstanceId, Executor assignedExecutor) {
        String configuration = swimlane.getDelegation() == null ? "" : swimlane.getDelegation().getConfiguration();
        id = swimlane.getId();
        this.processInstanceId = processInstanceId;
        name = swimlane.getName();
        isAssigned = assignedExecutor != null;
        executor = assignedExecutor;
        orgFunction = configuration;
    }

    public static SwimlaneStub createBlankSwimlaneStub(Long processInstanceId) {
        return new SwimlaneStub(processInstanceId);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public Executor getExecutor() {
        if (!isAssigned) {
            throw new IllegalStateException("Swimlane is unassigned");
        }
        return executor;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public String getOrgFunction() {
        return orgFunction;
    }
}

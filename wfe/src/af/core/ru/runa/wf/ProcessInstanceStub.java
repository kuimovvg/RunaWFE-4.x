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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import ru.runa.af.IdentifiableBaseImpl;
import ru.runa.bpm.graph.exe.ProcessInstance;

/**
 * Created on 02.11.2004
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceStub", namespace = "http://runa.ru/workflow/webservices")
public class ProcessInstanceStub extends IdentifiableBaseImpl implements Serializable {
    private static final long serialVersionUID = 4862220986262286596L;

    private Long id;

    private String name;

    private Date startDate;

    private Date endDate;

    private int version;

    private Long processDefinitionNativeId;

    protected String hierarchySubProcess;

    public ProcessInstanceStub() {
    }

    public ProcessInstanceStub(ProcessInstance processInstance) {
        id = processInstance.getId();
        name = processInstance.getProcessDefinition().getName();
        processDefinitionNativeId = processInstance.getProcessDefinition().getId();
        version = processInstance.getProcessDefinition().getVersion().intValue();
        startDate = processInstance.getStartDate();
        endDate = processInstance.getEndDate();
        hierarchySubProcess = processInstance.getHierarchySubProcess();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * @return true if process instance is ended.
     */
    public boolean isEnded() {
        return endDate != null;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getVersion() {
        return version;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProcessInstanceStub)) {
            return false;
        }
        ProcessInstanceStub instance = (ProcessInstanceStub) obj;

        if (getId() == instance.getId()) {
            return true;
        }
        return false;
    }

    private int hashCode = 0;

    public int hashCode() {
        if (hashCode == 0) {
            hashCode = 17;
            hashCode = 37 * hashCode + (int) (getId() ^ (getId() >>> 32));
        }
        return hashCode;
    }

    public String toString() {
        return "Definition name: " + name + ", instance id: " + id;
    }

    public Long getProcessDefinitionNativeId() {
        return processDefinitionNativeId;
    }

    public String getHierarchySubProcess() {
        return hierarchySubProcess;
    }
}

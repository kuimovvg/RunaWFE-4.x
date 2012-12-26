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
package ru.runa.wfe.execution.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

/**
 * Created on 02.11.2004
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WfProcess", namespace = "http://runa.ru/workflow/webservices")
public class WfProcess implements Identifiable {
    private static final long serialVersionUID = 4862220986262286596L;
    public static final String SELECTED_TRANSITION_KEY = "_SELECTED_TRANSITION_";

    private Long id;
    private String name;
    private Date startDate;
    private Date endDate;
    private int version;
    private Long processDefinitionId;
    private String hierarchySubProcess;

    public WfProcess() {
    }

    public WfProcess(Process process) {
        this.id = process.getId();
        this.name = process.getDefinition().getName();
        this.processDefinitionId = process.getDefinition().getId();
        this.version = process.getDefinition().getVersion().intValue();
        this.startDate = process.getStartDate();
        this.endDate = process.getEndDate();
        this.hierarchySubProcess = process.getHierarchySubProcess();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.PROCESS;
    }

    public String getName() {
        return name;
    }

    /**
     * @return true if process is ended.
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

    @Override
    public String toString() {
        return name + ": " + id;
    }

    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getHierarchySubProcess() {
        return hierarchySubProcess;
    }
}

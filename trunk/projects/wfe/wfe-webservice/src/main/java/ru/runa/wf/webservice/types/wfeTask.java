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
package ru.runa.wf.webservice.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;

@XmlType(name = "wfeTask", namespace = "http://runa.ru/workflow/webservices")
@XmlAccessorType(XmlAccessType.FIELD)
public class wfeTask {

    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    Long taskId;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    String name;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    String description;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    Executor owner;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    Long processId;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    Long processDefinitionId;

    public wfeTask(WfTask task) {
        taskId = task.getId();
        name = task.getName();
        description = task.getDescription();
        owner = task.getOwner();
        processId = task.getProcessId();
        processDefinitionId = task.getDefinitionId();
    }

    /**
     * This need for web services
     */
    protected wfeTask() {
    }
}

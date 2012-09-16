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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.af.IdentifiableBaseImpl;
import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.par.FileDataProvider;

/**
 * Created on 29.09.2004
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDefinition", namespace = "http://runa.ru/workflow/webservices")
public class ProcessDefinition extends IdentifiableBaseImpl implements Serializable {
    private static final long serialVersionUID = -6032491529439317948L;

    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private Long id;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private Long nativeId; // TODO delete this
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private String name;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private String description;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private String[] type;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private Long version;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private boolean hasHtmlDescription;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private boolean hasStartImage;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private boolean hasDisabledImage;

    public ProcessDefinition() {
    }

    public ProcessDefinition(ExecutableProcessDefinition definition) {
        this(definition.getDBImpl());
        this.id = new Long(definition.getName().hashCode());
        this.hasHtmlDescription = definition.getFileBytes(FileDataProvider.INDEX_FILE_NAME) != null;
        this.hasStartImage = definition.getFileBytes(FileDataProvider.START_IMAGE_FILE_NAME) != null;
        this.hasDisabledImage = definition.getFileBytes(FileDataProvider.START_DISABLED_IMAGE_FILE_NAME) != null;
    }

    public ProcessDefinition(ArchievedProcessDefinition definition) {
        this.nativeId = definition.getId();
        this.version = definition.getVersion();
        this.name = definition.getName();
        this.description = null;
        this.version = definition.getVersion();
    }

    public void setType(String[] type) {
        this.type = type;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getVersion() {
        return version;
    }

    public Long getNativeId() {
        return nativeId;
    }

    public String[] getType() {
        return type;
    }

    public boolean hasHtmlDescription() {
        return hasHtmlDescription;
    }

    public boolean hasStartImage() {
        return hasStartImage;
    }

    public boolean hasDisabledImage() {
        return hasDisabledImage;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", version: " + version;
    }
}

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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;

@Entity
@Table(name = "PROCESS_DEFINITION_INFO")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProcessDefinitionInfo {
    private Long id;
    private String processName;
    private String[] processType;

    // For Hibernate 
    protected ProcessDefinitionInfo() {
    }

    public ProcessDefinitionInfo(String processName, String[] processType) {
        this.processName = processName;
        this.processType = processType;
    }

    @CollectionOfElements
    @JoinTable(name = "PROCESS_TYPES", joinColumns = { @JoinColumn(name = "PROCESS_NAME", nullable = false, updatable = false) })
    @IndexColumn(name = "ARRAY_INDEX")
    @Column(name = "TYPE")
    public String[] getProcessType() {
        return processType;
    }

    public void setProcessType(String[] processType) {
        this.processType = processType;
    }

    @Column(name = "PROCESS_NAME", length = 128, nullable = false)
    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_PROCESS_DEFINITION_INFO")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "SORT_COLUMN", length = 128, nullable = false)
    public String getProcessTypeHibernate() {
        String result = "";
        for (String str : processType) {
            result = result + "/" + str;
        }
        return result;
    }

    public void setProcessTypeHibernate(String processTypeHibernate) {
    }
}

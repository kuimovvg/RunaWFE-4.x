/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.taskmgmt.exe;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.commons.EqualsUtil;

/**
 * is a process role for a one process instance.
 */
@Entity
@Table(name = "JBPM_SWIMLANEINSTANCE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SwimlaneInstance implements Serializable, Assignable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private String name;
    private String assignedActorId;
    private TaskMgmtInstance taskMgmtInstance;

    private Swimlane swimlane;

    public SwimlaneInstance() {
    }

    public SwimlaneInstance(Swimlane swimlane) {
        this.name = swimlane.getName();
        this.swimlane = swimlane;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_SWIMLANEINSTANCE")
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION_")
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME_")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "ACTORID_")
    public String getAssignedActorId() {
        return assignedActorId;
    }

    public void setAssignedActorId(String assignedActorId) {
        this.assignedActorId = assignedActorId;
    }

    @Transient
    public Swimlane getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(Swimlane swimlane) {
        this.swimlane = swimlane;
    }

    @ManyToOne(targetEntity = TaskMgmtInstance.class)
    @JoinColumn(name = "TASKMGMTINSTANCE_")
    @ForeignKey(name = "FK_SWIMLANEINST_TM")
    public TaskMgmtInstance getTaskMgmtInstance() {
        return taskMgmtInstance;
    }

    public void setTaskMgmtInstance(TaskMgmtInstance taskMgmtInstance) {
        this.taskMgmtInstance = taskMgmtInstance;
    }

    // TODO equals
    // hack to support comparing hibernate proxies against the real objects
    // since this always falls back to ==, we don't need to overwrite the
    // hashcode
    @Override
    public boolean equals(Object o) {
        return EqualsUtil.equals(this, o);
    }

    @Override
    public void setActorId(ExecutionContext executionContext, String actorId) {
        setActorId(executionContext, actorId, true);
    }

    public void setActorId(ExecutionContext executionContext, String actorId, boolean sync) {
        this.assignedActorId = actorId;
        if (sync) {
            syncWithVariable(executionContext);
        }
    }

    public void setTaskMgmtInstanceWithSync(ExecutionContext executionContext, TaskMgmtInstance taskMgmtInstance) {
        this.taskMgmtInstance = taskMgmtInstance;
        syncWithVariable(executionContext);
    }

    private void syncWithVariable(ExecutionContext executionContext) {
        if (assignedActorId == null || taskMgmtInstance == null) {
            return;
        }
        taskMgmtInstance.getProcessInstance().getContextInstance().setVariable(executionContext, name, assignedActorId);
    }
}

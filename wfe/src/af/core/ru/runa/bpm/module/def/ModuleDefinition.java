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
package ru.runa.bpm.module.def;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.module.exe.ModuleInstance;

@Entity
@Table(name = "JBPM_MODULEDEFINITION")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CLASS_", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "M")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class ModuleDefinition {
    private Long id;
    private String name;
    private ArchievedProcessDefinition processDefinition;

    public ModuleDefinition() {
    }

    public abstract ModuleInstance createInstance();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_MODULEDEFINITION")
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME_")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(targetEntity = ArchievedProcessDefinition.class)
    @JoinColumn(name = "PROCESSDEFINITION_", nullable = false)
    @ForeignKey(name = "FK_MODDEF_PROCDEF")
    @Index(name = "IDX_MODDEF_PROCDF")
    public ArchievedProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ArchievedProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

}

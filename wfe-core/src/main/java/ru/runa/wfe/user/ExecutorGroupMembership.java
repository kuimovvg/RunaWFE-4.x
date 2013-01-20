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
package ru.runa.wfe.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import com.google.common.base.Objects;

/**
 * Created on 02.02.2006
 * 
 */
@Entity
@Table(name = "EXECUTOR_GROUP_MEMBER", uniqueConstraints = @UniqueConstraint(columnNames = { "EXECUTOR_ID", "GROUP_ID" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ExecutorGroupMembership {
    private Long id;
    private Long version;
    private Group group;
    private Executor executor;

    public ExecutorGroupMembership() {
    }

    public ExecutorGroupMembership(Group group, Executor executor) {
        this.group = group;
        this.executor = executor;
    }

    @ManyToOne(targetEntity = Executor.class)
    @JoinColumn(name = "EXECUTOR_ID", nullable = false, insertable = true, updatable = false)
    @ForeignKey(name = "FK_EGM_EXECUTOR")
    @Index(name = "EXEC_GROUP_REL_EXEC_ID_IDX")
    @Fetch(FetchMode.JOIN)
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @ManyToOne(targetEntity = Group.class)
    @JoinColumn(name = "GROUP_ID", nullable = false, insertable = true, updatable = false)
    @ForeignKey(name = "FK_EGM_GROUP")
    @Index(name = "EXEC_GROUP_REL_GROUP_ID_IDX")
    @Fetch(FetchMode.JOIN)
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_EXECUTOR_GROUP_MEMBER")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION", nullable = false)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExecutorGroupMembership)) {
            return false;
        }
        ExecutorGroupMembership r = (ExecutorGroupMembership) obj;
        return Objects.equal(executor, r.executor) && Objects.equal(group, r.group);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(executor, group);
    }
}

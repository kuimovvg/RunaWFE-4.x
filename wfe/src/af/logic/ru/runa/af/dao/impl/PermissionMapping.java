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
package ru.runa.af.dao.impl;

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
import org.hibernate.annotations.Index;

import ru.runa.af.Executor;
import ru.runa.af.SecuredObject;

import com.google.common.base.Objects;

/**
 */
@Entity
@Table(name = "PERMISSION_MAPPINGS", uniqueConstraints = @UniqueConstraint(columnNames = { "SECURED_OBJECT_ID", "EXECUTOR_ID", "MASK" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PermissionMapping {
    private Long id;

    private Long version;

    private SecuredObject securedObject;

    private Executor executor;

    private Long mask;

    protected PermissionMapping() {
    }

    public PermissionMapping(Executor executor, SecuredObject securedObject) {
        setExecutor(executor);
        setSecuredObject(securedObject);
    }

    public PermissionMapping(Executor executor, SecuredObject securedObject, Long mask) {
        setExecutor(executor);
        setSecuredObject(securedObject);
        setMask(mask);
    }

    @ManyToOne(targetEntity = SecuredObject.class)
    @JoinColumn(name = "SECURED_OBJECT_ID", nullable = false)
    @Index(name = "PERM_MAPPINGS_SEC_OBJ_ID_IDX")
    @Fetch(FetchMode.JOIN)
    public SecuredObject getSecuredObject() {
        return securedObject;
    }

    private void setSecuredObject(SecuredObject securedObject) {
        this.securedObject = securedObject;
    }

    @ManyToOne(targetEntity = Executor.class)
    @JoinColumn(name = "EXECUTOR_ID", nullable = false)
    @Index(name = "PERM_MAPPINGS_EXEC_ID_IDX")
    @Fetch(FetchMode.JOIN)
    public Executor getExecutor() {
        return executor;
    }

    private void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Column(name = "MASK", nullable = false)
    public Long getMask() {
        return mask;
    }

    public void setMask(Long mask) {
        this.mask = mask;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PermissionMapping)) {
            return false;
        }
        PermissionMapping pm = (PermissionMapping) obj;
        return Objects.equal(mask, pm.mask) && Objects.equal(executor, pm.executor) && Objects.equal(securedObject, pm.securedObject);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mask, executor, securedObject);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_PERMISSION_MAPPINGS")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION", nullable = false)
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }
}

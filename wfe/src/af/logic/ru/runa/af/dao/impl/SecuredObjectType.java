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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import ru.runa.af.Executor;

import com.google.common.base.Objects;

/*
 * Created on 15.12.2004
 */
/**
 */
@Entity
@Table(name = "SECURED_OBJECT_TYPES")
//@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class SecuredObjectType {

    private Long id;

    private String permissionClassName;

    private Set<Executor> privelegedExecutors;

    private int type;

    protected SecuredObjectType() {
    }

    public SecuredObjectType(int typeCode, String permissionClassName, Set<Executor> privelegedExecutors) {
        setPrivelegedExecutors(privelegedExecutors);
        setType(typeCode);
        setPermissionClassName(permissionClassName);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_SECURED_OBJECT_TYPES")
    @Column(name = "ID", nullable = false)
    protected Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Column(name = "PERMISSION_CLASS_NAME", nullable = false)
    public String getPermissionClassName() {
        return permissionClassName;
    }

    @ManyToMany(targetEntity = Executor.class)
    @Sort(type = SortType.UNSORTED)
    @JoinTable(name = "PRIVELEGE_MAPPINGS", joinColumns = { @JoinColumn(name = "TYPE_CODE", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "EXECUTOR_ID", nullable = false, updatable = false) })
    @Cascade( { CascadeType.SAVE_UPDATE })
    @Fetch(FetchMode.JOIN)
    public Set<Executor> getPrivelegedExecutors() {
        return privelegedExecutors;
    }

    @Column(name = "TYPE_CODE", unique = true)
    public int getType() {
        return type;
    }

    private void setPermissionClassName(String permissionClassName) {
        this.permissionClassName = permissionClassName;
    }

    private void setPrivelegedExecutors(Set<Executor> privelegedExecutors) {
        this.privelegedExecutors = privelegedExecutors;
    }

    private void setType(int typeCode) {
        type = typeCode;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SecuredObjectType)) {
            return false;
        }
        SecuredObjectType type = (SecuredObjectType) obj;
        return Objects.equal(getType(), type.getType()) && Objects.equal(getPermissionClassName(), type.getPermissionClassName())
                && Objects.equal(getPrivelegedExecutors(), type.getPrivelegedExecutors());
    }

    public int hashCode() {
        return Objects.hashCode(getType(), getPermissionClassName(), getPrivelegedExecutors());
    }
}

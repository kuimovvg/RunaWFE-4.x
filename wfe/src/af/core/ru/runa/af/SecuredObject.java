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
package ru.runa.af;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

/**
 * Is an element of system which user interacts with given permissions.
 * 
 */
@Entity
@Table(name = "SECURED_OBJECTS", uniqueConstraints = @UniqueConstraint(columnNames = { "EXT_ID", "TYPE_CODE" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SecuredObject {

    private Long id;

    private Long extId;

    private int type;

    public SecuredObject() {
    }

    public SecuredObject(Long extId, int type) {
        setExtId(extId);
        setType(type);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_SECURED_OBJECTS")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    /**
     * @return an id of object which this instance represents.
     */
    @Column(name = "EXT_ID", nullable = false)
    public Long getExtId() {
        return extId;
    }

    @Column(name = "TYPE_CODE", nullable = false)
    public int getType() {
        return type;
    }

    private void setExtId(Long extId) {
        this.extId = extId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private void setType(int type) {
        this.type = type;
    }

    private Long version;

    @Version
    @Column(name = "VERSION", nullable = false)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + (int) (extId ^ (extId >>> 32));
//        result = prime * result + type;
        return Objects.hashCode(extId, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SecuredObject other = (SecuredObject) obj;
        return Objects.equal(extId, other.extId) && Objects.equal(type, other.type);
    }
}

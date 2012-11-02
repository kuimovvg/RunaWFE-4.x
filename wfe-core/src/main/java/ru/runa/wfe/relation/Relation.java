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
package ru.runa.wfe.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

import com.google.common.base.Objects;

/**
 * Relation between executors. Each relation contains some RelationPair, which describe executors relation.
 */
@Entity
@Table(name = "RELATION_GROUP")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Relation implements Identifiable {
    private static final long serialVersionUID = 1L;

    /**
     * Identity of relation. This field is set, then relation is stored in database.
     */
    private Long id;

    /**
     * Name of relation.
     */
    private String name;

    /**
     * Description of relation.
     */
    private String description;

    public Relation() {
    }

    /**
     * Create instance of relation with given name and description.
     * 
     * @param name
     *            Name of relation.
     * @param description
     *            Description of relation.
     */
    public Relation(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.RELATION;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_RELATION_GROUP")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    /**
     * Return name of relation.
     * 
     * @return Name of relation.
     */
    @Column(name = "NAME", unique = true)
    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Return description of relation.
     * 
     * @return Description of relation.
     */
    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Relation other = (Relation) obj;
        return Objects.equal(name, other.name);
    }

}

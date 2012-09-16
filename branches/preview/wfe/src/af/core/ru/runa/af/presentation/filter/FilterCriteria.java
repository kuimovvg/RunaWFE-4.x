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
package ru.runa.af.presentation.filter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.PolymorphismType;
import org.hibernate.criterion.Criterion;

import ru.runa.af.util.QueryParameter;

import com.google.common.base.Objects;

/**
 * Created 01.09.2005
 * 
 */
@Entity
@org.hibernate.annotations.Entity(polymorphism = PolymorphismType.IMPLICIT)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CRITERIA_TYPE", discriminatorType = DiscriminatorType.STRING, length = 255)
@Table(name = "FILTER_CRITERIAS")
public abstract class FilterCriteria implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long version;

    protected String[] filterTemplates;

    protected int templatesCount;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_FILTER_CRITERIAS")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public int getTemplatesCount() {
        return templatesCount;
    }

    protected abstract void validate(String[] newTemplates) throws FilterFormatException;

    @Version
    @Column(name = "VERSION", nullable = false)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @CollectionOfElements
    @JoinTable(name = "CRITERIA_CONDITIONS", joinColumns = { @JoinColumn(name = "FILTER_CRITERIA_ID", nullable = false, updatable = false) })
    @IndexColumn(name = "CONDITION_INDEX")
    @Column(name = "CRITERIA_CONDITION", updatable = false)
    public String[] getFilterTemplates() {
        return filterTemplates;
    }

    protected void setFilterTemplates(String[] filterTemplates) {
        this.filterTemplates = filterTemplates;
    }

    public void applyFilterTemplates(String[] filterTemplates) throws FilterFormatException {
        validate(filterTemplates);
        this.filterTemplates = filterTemplates;
    }

    public abstract String buildWhereCondition(String fieldName, String persistetObjectQueryAlias, Map<String, QueryParameter> placeholders);

    public abstract void buildWhereClausePart(StringBuilder query, String persistetObjectFieldName, String persistetObjectQueryAlias,
            Map<String, Object> queryNamedParameterNameValueMap);

    public abstract Criterion buildCriterion(String fieldName);

    @Override
    public FilterCriteria clone() {
        try {
            FilterCriteria clone = (FilterCriteria) super.clone();
            clone.id = null;
            clone.version = null;
            clone.filterTemplates = filterTemplates.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Clone error");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        if (Arrays.equals(((FilterCriteria) obj).filterTemplates, filterTemplates)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode((Object[]) filterTemplates);
    }
}

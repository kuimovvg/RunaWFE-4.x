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
package ru.runa.wfe.ss;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import ru.runa.wfe.commons.OracleCommons;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;

/**
 * Criterion for applying substitution rule.
 */
@Entity
@Table(name = "SUBSTITUTION_CRITERIA", uniqueConstraints = @UniqueConstraint(columnNames = { "DISCRIMINATOR", "CONF" }))
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SubstitutionCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String conf;

    private String DISPLAY_TYPE = "";

    public SubstitutionCriteria() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_SUBSTITUTION_CRITERIA")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    // This is need by hibernate.
    protected void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = OracleCommons.fixNullString(name);
    }

    @Column(name = "CONF")
    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = OracleCommons.fixNullString(conf);
    }

    @Override
    public String toString() {
        return name + ", " + (conf != null ? conf : "");
    }

    public boolean isSatisfied(ExecutionContext executionContext, Task task, Actor asActor, Actor substitutorActor) {
        return false;
    }

    public boolean validate() {
        return false;
    }

    public String displayType() {
        return DISPLAY_TYPE;
    }
}

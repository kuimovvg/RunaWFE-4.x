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
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Index;

import ru.runa.af.util.OracleCommons;

import com.google.common.base.Objects;

/**
 * Actor represents a real user of system that could perform different actions.
 */
@Entity
@DiscriminatorValue(value = "N")
@XmlType(name = "Actor", namespace = "http://runa.ru/workflow/webservices")
@XmlAccessorType(XmlAccessType.FIELD)
public class Actor extends Executor {

    private static final long serialVersionUID = -582492651083909598L;

    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private Long code;

    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private boolean isActive = true;

    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private String email;

    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private String phone;

    protected Actor() {
    }

    /**
     * Creates an {@link Actor}
     * 
     * @param name {@link Actor}name
     * @param description {@link Actor}description. If description is null, constructor changes it to empty String value
     * @throws NullPointerException if {@link Actor}name is null
     */
    public Actor(String name, String description) {
        this(name, description, null);
    }

    public Actor(String name, String description, String fullName) {
        this(name, description, fullName, null);
    }

    public Actor(String name, String description, String fullName, Long code) {
        super(name, description);
        setFullName(fullName == null ? "" : fullName);
        setCode(code);
    }

    public Actor(String name, String description, String fullName, Long code, String email, String phone) {
        this(name, description, fullName, code);
        this.email = email;
        this.phone = phone;
    }

    @Column(name = "FULL_NAME", nullable = false)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = OracleCommons.fixNullString(fullName);
    }

    @Column(name = "CODE")
    @Index(name = "EXECUTORS_CODE_IDX")
    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Column(name = "E_MAIL")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "PHONE", length = 32)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public void update(Executor updateFrom) {
        super.update(updateFrom);
        Actor other = (Actor) updateFrom;
        setActive(other.isActive());
        setCode(other.getCode());
        setEmail(other.getEmail());
        setPhone(other.getPhone());
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Actor)) {
            return false;
        }
        Actor actor = (Actor) obj;
        return Objects.equal(code, actor.code) && Objects.equal(fullName, actor.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getFullName(), getCode());
    }

}

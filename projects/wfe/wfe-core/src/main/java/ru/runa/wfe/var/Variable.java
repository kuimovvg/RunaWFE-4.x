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
package ru.runa.wfe.var;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.VariableCreateLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableUpdateLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;

/**
 * Base class for classes that store variable values in the database.
 */
@Entity
@Table(name = "BPM_VARIABLE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Variable<T extends Object> {
    protected Long id;
    private Long version;
    private String name;
    private Process process;
    private Converter converter;

    public Variable() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_VARIABLE")
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION")
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "CONVERTER")
    @Type(type = "ru.runa.wfe.commons.hibernate.ConverterEnumType")
    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_VARINST_PRCINST")
    @Index(name = "IDX_VARINST_PRCINS")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * is true if this variable supports the given value, false otherwise.
     */
    public abstract boolean isStorable(Object value);

    /**
     * Get the value of the variable.
     */
    @Transient
    protected abstract T getStorableValue();

    /**
     * Set new variable value
     */
    protected abstract void setStorableValue(T object);

    private void addLog(ExecutionContext executionContext, Object oldValue, Object newValue) {
        if (oldValue == null) {
            executionContext.addLog(new VariableCreateLog(this, newValue));
        } else if (newValue == null) {
            executionContext.addLog(new VariableDeleteLog(this));
        } else {
            executionContext.addLog(new VariableUpdateLog(this, oldValue, newValue));
        }
    }

    public boolean supports(Object value) {
        if (converter != null) {
            return converter.supports(value);
        }
        return isStorable(value);
    }

    public void setValue(ExecutionContext executionContext, Object newValue) {
        Object newStorableValue;
        if (converter != null) {
            if (!converter.supports(newValue)) {
                throw new InternalApplicationException("the converter '" + converter.getClass().getName() + "' in variable '"
                        + this.getClass().getName() + "' does not support values of type '" + newValue.getClass().getName() + "'.");
            }
            newStorableValue = converter.convert(newValue);
        } else {
            newStorableValue = newValue;
        }
        if (newStorableValue != null && !this.isStorable(newStorableValue)) {
            throw new InternalApplicationException("variable '" + this.getClass().getName() + "' does not support values of type '"
                    + newStorableValue.getClass().getName() + "'.");
        }
        Object oldValue = getStorableValue();
        if (converter != null && oldValue != null) {
            oldValue = converter.revert(oldValue);
        }
        addLog(executionContext, oldValue, newValue);
        setStorableValue((T) newStorableValue);
    }

    @Transient
    public Object getValue() {
        Object value = getStorableValue();
        if (value != null && converter != null) {
            value = converter.revert(value);
        }
        return value;
    }

    public String toString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public String toString() {
        return toString(getValue());
    }

}

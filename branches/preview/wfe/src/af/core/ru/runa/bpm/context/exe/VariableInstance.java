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
package ru.runa.bpm.context.exe;

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
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.bpm.taskmgmt.exe.SwimlaneInstance;

/**
 * is a jbpm-internal class that serves as a base class for classes that store
 * variable values in the database.
 */
@Entity
@Table(name = "JBPM_VARIABLEINSTANCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CLASS_", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class VariableInstance<T extends Object> {
    protected Long id;
    private Long version;
    private String name;
    protected Token token;
    private TokenVariableMap tokenVariableMap;
    private ProcessInstance processInstance;
    private Converter converter;
    // TODO these 2 are really need?
    private Object valueCache;
    private boolean valueCached;

    public VariableInstance() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_VARIABLEINSTANCE")
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION_")
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME_")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "CONVERTER_")
    @Type(type = "ru.runa.bpm.db.hibernate.ConverterEnumType")
    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @ManyToOne(targetEntity = Token.class)
    @JoinColumn(name = "TOKEN_", nullable = false)
    @ForeignKey(name = "FK_VARINST_TK")
    @Index(name = "IDX_VARINST_TK")
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @ManyToOne(targetEntity = TokenVariableMap.class)
    @JoinColumn(name = "TOKENVARIABLEMAP_"/* TODO, nullable = false */)
    @ForeignKey(name = "FK_VARINST_TKVARMP")
    @Index(name = "IDX_VARINST_TKVARMP")
    public TokenVariableMap getTokenVariableMap() {
        return tokenVariableMap;
    }

    public void setTokenVariableMap(TokenVariableMap tokenVariableMap) {
        this.tokenVariableMap = tokenVariableMap;
    }

    @ManyToOne(targetEntity = ProcessInstance.class)
    @JoinColumn(name = "PROCESSINSTANCE_", nullable = false)
    @ForeignKey(name = "FK_VARINST_PRCINST")
    @Index(name = "IDX_VARINST_PRCINS")
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    /**
     * is true if this variable-instance supports the given value, false
     * otherwise.
     */
    public abstract boolean isStorable(Object value);

    /**
     * is the value, stored by this variable instance.
     */
    @Transient
    protected abstract T getObject();

    /**
     * stores the value in this variable instance.
     */
    protected abstract void setNewValue(T value);

    // variable management
    // //////////////////////////////////////////////////////

    public boolean supports(Object value) {
        if (converter != null) {
            return converter.supports(value);
        }
        return isStorable(value);
    }

    public void setValue(Object value) {
        valueCache = value;
        valueCached = true;

        if (converter != null) {
            if (!converter.supports(value)) {
                throw new InternalApplicationException("the converter '" + converter.getClass().getName() + "' in variable instance '"
                        + this.getClass().getName() + "' does not support values of type '" + value.getClass().getName()
                        + "'.  to change the type of a variable, you have to delete it first");
            }
            value = converter.convert(value);
        }
        if ((value != null) && (!this.isStorable(value))) {
            throw new InternalApplicationException("variable instance '" + this.getClass().getName() + "' does not support values of type '"
                    + value.getClass().getName() + "'.  to change the type of a variable, you have to delete it first");
        }
        setNewValue((T) value);
    }

    @Transient
    public Object getValue() {
        if (valueCached) {
            return valueCache;
        }
        Object value = getObject();
        if (value != null && converter != null) {
            value = converter.revert(value);
            valueCache = value;
            valueCached = true;
        }
        return value;
    }

    public void removeReferences() {
        tokenVariableMap = null;
        token = null;
        processInstance = null;
    }

    // utility methods /////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "${" + name + "}";
    }

    public void syncWithSwimlane(ExecutionContext executionContext) {
        if (name == null || valueCache == null) {
            return;
        }
        Swimlane swimlane = executionContext.getProcessDefinition().getSwimlane(name);
        if (swimlane == null) {
            return;
        }
        SwimlaneInstance swimlaneInst = processInstance.getTaskMgmtInstance().getSwimlaneInstance(name);
        if (swimlaneInst != null && valueCache.equals(swimlaneInst.getAssignedActorId())) {
            return;
        }
        if (swimlaneInst == null) {
            swimlaneInst = new SwimlaneInstance(swimlane);
            processInstance.getTaskMgmtInstance().addSwimlaneInstance(executionContext, swimlaneInst);
        }
        swimlaneInst.setActorId(executionContext, valueCache.toString(), false);
    }

}

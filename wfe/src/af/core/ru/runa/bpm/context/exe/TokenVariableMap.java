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

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKey;

import ru.runa.bpm.graph.exe.Token;

/**
 * is a jbpm-internal map of variableInstances related to one {@link Token}.  
 * Each token has it's own map of variableInstances, thereby creating 
 * hierarchy and scoping of process variableInstances. 
 */
@Entity
@Table(name = "JBPM_TOKENVARIABLEMAP")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TokenVariableMap extends VariableContainer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    protected Token token = null;
    protected ContextInstance contextInstance = null;

    public TokenVariableMap() {
    }

    public TokenVariableMap(Token token, ContextInstance contextInstance) {
        this.token = token;
        this.contextInstance = contextInstance;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_TOKENVARIABLEMAP")
    @Column(name = "ID_", nullable = false)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION_", nullable = false)
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @ManyToOne(targetEntity = ContextInstance.class)
    @JoinColumn(name = "CONTEXTINSTANCE_", nullable = false, insertable = true, updatable = false)
    @ForeignKey(name = "FK_TKVARMAP_CTXT")
    @Index(name = "IDX_TKVARMAP_CTXT")
    @Fetch(FetchMode.JOIN)
    @Override
    public ContextInstance getContextInstance() {
        return contextInstance;
    }
    
    public void setContextInstance(ContextInstance contextInstance) {
        this.contextInstance = contextInstance;
    }

    @ManyToOne(targetEntity = Token.class)
    @JoinColumn(name = "TOKEN_", nullable = false, updatable = false)
    @ForeignKey(name = "FK_TKVARMAP_TOKEN")
    @Index(name = "IDX_TKVVARMP_TOKEN")
    @Fetch(FetchMode.JOIN)
    @Override
    public Token getToken() {
        return token;
    }
    
    public void setToken(Token token) {
        this.token = token;
    }

    @OneToMany(fetch = FetchType.EAGER, targetEntity = VariableInstance.class)
    @JoinColumn(name = "TOKENVARIABLEMAP_")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @MapKey(targetElement = String.class, columns = @Column(name = "NAME_"))
    @Override
    public Map<String, VariableInstance<?>> getVariableInstances() {
        return variableInstances;
    }

    @Override
    public void addLocalVariable(VariableInstance<?> variableInstance) {
        super.addLocalVariable(variableInstance);
        variableInstance.setTokenVariableMap(this);
    }

    @Override
    public String toString() {
        return "TokenVariableMap" + ((token != null) ? "[" + token.getFullName() + "]" : "@" + Integer.toHexString(System.identityHashCode(this)));
    }

    @Transient
    @Override
    protected VariableContainer getParentVariableContainer() {
        Token parentToken = token.getParent();
        if (parentToken == null) {
            return null;
        }
        return contextInstance.getTokenVariableMap(parentToken);
    }

}

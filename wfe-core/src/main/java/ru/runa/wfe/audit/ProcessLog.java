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
package ru.runa.wfe.audit;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Base class for logging process unit of work.
 * 
 * @author Dofs
 */
@Entity
@Table(name = "BPM_LOG")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ProcessLog implements IAttributes, Serializable, Comparable<ProcessLog> {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long processId;
    private Long tokenId;
    private Date date;
    private Severity severity = Severity.DEBUG;
    private HashMap<String, String> attributes = Maps.newHashMap();
    private byte[] bytes;
    private String nodeId;

    public ProcessLog() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_LOG")
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "PROCESS_ID", nullable = false)
    @Index(name = "IX_LOG_PROCESS")
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Column(name = "TOKEN_ID")
    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    @Column(name = "NODE_ID")
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "LOG_DATE", nullable = false)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Column(name = "SEVERITY", nullable = false)
    @Enumerated(EnumType.STRING)
    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Column(name = "CONTENT", length = 4000)
    public String getContent() {
        return XmlUtils.serialize(attributes);
    }

    public void setContent(String content) {
        attributes = XmlUtils.deserialize(content);
    }

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 16777216, name = "BYTES")
    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @return bytes implementation (if any).
     */
    @Transient
    public Object getBytesObject() {
        return getBytes();
    }

    protected void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    protected void addAttributeWithTruncation(String name, String value, int maxLength) {
        if (value.length() > maxLength) {
            value = value.substring(0, maxLength) + "...";
        }
        addAttribute(name, value);
    }

    protected String getAttribute(String name) {
        return attributes.get(name);
    }

    protected String getAttributeNotNull(String name) {
        String s = getAttribute(name);
        Preconditions.checkNotNull(s, name);
        return s;
    }

    /**
     * @return Arguments for localized pattern to format log message
     *         description.
     */
    @Transient
    public abstract Object[] getPatternArguments();

    /**
     * Formats log message description
     * 
     * @param pattern
     *            localized pattern
     * @return formatted message
     */
    public final String toString(String pattern, Object... arguments) {
        return MessageFormat.format(pattern, arguments);
    }

    @Override
    public int compareTo(ProcessLog o) {
        return date.compareTo(o.date);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("nodeId", nodeId).add("tokenId", tokenId)
                .add("date", CalendarUtil.formatDateTime(date)).add("attributes", attributes).toString();
    }
}
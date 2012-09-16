package ru.runa.bpm.graph.def;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "JBPM_PROCESSDEFINITION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ArchievedProcessDefinition {
    private Long id;
    private Long version;
    private String name;
    private String description;
    private byte[] parFile;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_PROCESSDEFINITION")
    @Column(name = "ID_", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "VERSION_", nullable = false)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME_", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DESCRIPTION_")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Lob
    @Column(name = "PAR_BYTES")
    public byte[] getParFile() {
        return parFile;
    }

    public void setParFile(byte[] parFile) {
        this.parFile = parFile;
    }

}

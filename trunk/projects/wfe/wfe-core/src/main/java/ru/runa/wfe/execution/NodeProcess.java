package ru.runa.wfe.execution;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.wfe.lang.Node;

@Entity
@Table(name = "BPM_SUBPROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NodeProcess {
    private Long id;
    private Process process;
    private Token parentToken;
    private String nodeId;
    private Process subProcess;

    protected NodeProcess() {
    }

    public NodeProcess(Token parentToken, Process child, Node processStateNode) {
        process = parentToken.getProcess();
        this.parentToken = parentToken;
        nodeId = processStateNode.getNodeId();
        subProcess = child;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_SUBPROCESS")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = Process.class)
    @JoinColumn(name = "PARENT_PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PARENT_PROCESS")
    @Index(name = "IDX_PARENT_PROCESS")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @ManyToOne(targetEntity = Token.class)
    @JoinColumn(name = "PARENT_TOKEN_ID")
    @ForeignKey(name = "FK_SUBPROCESS_TOKEN")
    @Fetch(FetchMode.JOIN)
    public Token getParentToken() {
        return parentToken;
    }

    public void setParentToken(Token parentToken) {
        this.parentToken = parentToken;
    }

    @ManyToOne(targetEntity = Process.class)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PROCESS")
    @Index(name = "IDX_PROCESS")
    @Cascade({ CascadeType.ALL })
    public Process getSubProcess() {
        return subProcess;
    }

    public void setSubProcess(Process subProcess) {
        this.subProcess = subProcess;
    }

    @Column(name = "PARENT_NODE_ID")
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

}

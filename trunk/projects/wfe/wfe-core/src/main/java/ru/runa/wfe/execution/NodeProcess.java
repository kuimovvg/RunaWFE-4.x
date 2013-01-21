package ru.runa.wfe.execution;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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

    public NodeProcess(Token parentToken, Process subProcess, Node processStateNode) {
        process = parentToken.getProcess();
        this.parentToken = parentToken;
        nodeId = processStateNode.getNodeId();
        this.subProcess = subProcess;
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

    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PARENT_PROCESS")
    @Index(name = "IX_SUBPROCESS_PARENT_PROCESS")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @ManyToOne(targetEntity = Token.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TOKEN_ID")
    @ForeignKey(name = "FK_SUBPROCESS_TOKEN")
    public Token getParentToken() {
        return parentToken;
    }

    public void setParentToken(Token parentToken) {
        this.parentToken = parentToken;
    }

    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PROCESS")
    @Index(name = "IX_SUBPROCESS_PROCESS")
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

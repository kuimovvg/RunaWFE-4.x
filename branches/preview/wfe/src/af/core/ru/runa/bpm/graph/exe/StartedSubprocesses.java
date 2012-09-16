package ru.runa.bpm.graph.exe;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.bpm.graph.def.Node;

@Entity
@Table(name = "JBPM_NODE_SUBPROC")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StartedSubprocesses {
    private Long id;
    private ProcessInstance subProcessInstance;
    private ProcessInstance processInstance;
    private Node node;

    protected StartedSubprocesses() {
    }

    public StartedSubprocesses(ProcessInstance parent, ProcessInstance child, Node processStateNode) {
        processInstance = parent;
        subProcessInstance = child;
        node = processStateNode;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_PASSTRANS")
    @Column(name = "ID_", nullable = false)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = ProcessInstance.class)
    @JoinColumn(name = "PROCESSINSTANCE_", nullable = false)
    @ForeignKey(name = "FK_NODE_SUBPROC_PROCINST")
    @Index(name = "IDX_NODE_SUBPROC_PROCINST")
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }
    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    @ManyToOne(targetEntity = ProcessInstance.class)
    @JoinColumn(name = "SUBPROCESSINSTANCE_", nullable = false)
    @ForeignKey(name = "FK_NODE_SUBPROC_SUBPROCINST")
    @Index(name = "IDX_NODE_SUBPROC_SUBPROCINST")
    public ProcessInstance getSubProcessInstance() {
        return subProcessInstance;
    }
    public void setSubProcessInstance(ProcessInstance subProcessInstance) {
        this.subProcessInstance = subProcessInstance;
    }

//    @ManyToOne(targetEntity = Node.class)
//    @JoinColumn(name = "NODE_", nullable = false)
//    @ForeignKey(name = "FK_NODE_SUBPROC_NODE")
    @Transient
    public Node getNode() {
        return node;
    }
    public void setNode(Node node) {
        this.node = node;
    }

}

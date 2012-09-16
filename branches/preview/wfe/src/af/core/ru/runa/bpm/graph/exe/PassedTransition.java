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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.bpm.graph.def.Transition;
import ru.runa.commons.EqualsUtil;

@Entity
@Table(name = "JBPM_PASSTRANS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PassedTransition {
    private Long id;
    private ProcessInstance processInstance;
    private Transition transition;

    protected PassedTransition() {
    }

    public PassedTransition(ProcessInstance processInstance, Transition transition) {
        this.processInstance = processInstance;
        this.transition = transition;
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
    @ForeignKey(name = "FK_PASSTRANS_PROCINST")
    @Index(name = "IDX_PASSTRANS_PRCINST")
    @Fetch(FetchMode.JOIN)
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }
    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    @Transient
    public Transition getTransition() {
        return transition;
    }
    public void setTransition(Transition transition) {
        this.transition = transition;
    }

    // TODO
    // hack to support comparing hibernate proxies against the real objects
    // since this always falls back to ==, we don't need to overwrite the hashcode
    @Override
    public boolean equals(Object o) {
        return EqualsUtil.equals(this, o);
    }

}

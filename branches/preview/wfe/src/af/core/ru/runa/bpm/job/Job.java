package ru.runa.bpm.job;

import java.util.Date;

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
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;

@Entity
@Table(name = "JBPM_JOB")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CLASS_", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "J")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Job {

    protected Long id;
    private Long version;
    protected Date dueDate;
    protected ProcessInstance processInstance;
    protected Token token;
    protected TaskInstance taskInstance;

    public Job() {
    }

    public Job(Token token) {
        this.token = token;
        this.processInstance = token.getProcessInstance();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_JOB")
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

    @Column(name = "DUEDATE_")
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @ManyToOne(targetEntity = ProcessInstance.class)
    @JoinColumn(name = "PROCESSINSTANCE_", nullable = false)
    @ForeignKey(name = "FK_JOB_PRINST")
    @Index(name = "IDX_JOB_PRINST")
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    @ManyToOne(targetEntity = Token.class)
    @JoinColumn(name = "TOKEN_")
    @ForeignKey(name = "FK_JOB_TOKEN")
    @Index(name = "IDX_JOB_TOKEN")
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @ManyToOne(targetEntity = TaskInstance.class)
    @JoinColumn(name = "TASKINSTANCE_")
    @ForeignKey(name = "FK_JOB_TSKINST")
    @Index(name = "IDX_JOB_TSKINST")
    public TaskInstance getTaskInstance() {
        return taskInstance;
    }

    public void setTaskInstance(TaskInstance taskInstance) {
        this.taskInstance = taskInstance;
    }

    public abstract boolean execute(ExecutionContext executionContext) throws Exception;

    @Override
    public String toString() {
        return "job[" + id + "]";
    }

}

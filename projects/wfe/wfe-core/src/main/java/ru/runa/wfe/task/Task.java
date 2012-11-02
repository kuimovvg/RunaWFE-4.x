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
package ru.runa.wfe.task;

import java.util.Date;

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
import javax.persistence.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.handler.assign.Assignable;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Preconditions;

/**
 * is one task that can be assigned to an actor (read: put in someones task list) and that can trigger the coninuation of execution of the token upon completion.
 */
@Entity
@Table(name = "BPM_TASK")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Task implements Assignable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Task.class);

    private Long id;
    private Long version;
    private String name;
    private String description;
    private Executor executor;
    private Date createDate;
    private Date endDate;
    private Date deadlineDate;
    private boolean firstOpen;
    private Token token;
    private Swimlane swimlane;
    private Process process;

    public Task() {
    }

    public Task(TaskDefinition taskDefinition) {
        setName(taskDefinition.getName());
        setDescription(taskDefinition.getDescription());
        setFirstOpen(true);
        setCreateDate(new Date());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_TASK")
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

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "CREATE_DATE")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "END_DATE")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Column(name = "DEADLINE_DATE")
    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    @Column(name = "FIRST_OPEN")
    public boolean isFirstOpen() {
        return firstOpen;
    }

    public void setFirstOpen(boolean firstOpen) {
        this.firstOpen = firstOpen;
    }

    @ManyToOne(targetEntity = Token.class)
    @JoinColumn(name = "TOKEN_ID")
    @ForeignKey(name = "FK_TASKINST_TOKEN")
    @Index(name = "IDX_TASKINST_TOKN")
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @ManyToOne(targetEntity = Swimlane.class)
    @JoinColumn(name = "SWIMLANE_ID")
    @ForeignKey(name = "FK_TASKINST_SLINST")
    @Index(name = "IDX_TSKINST_SLINST")
    public Swimlane getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(Swimlane swimlane) {
        this.swimlane = swimlane;
    }

    @ManyToOne(targetEntity = Process.class)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_TSKINS_PRCINS")
    @Index(name = "IDX_TASKINST_TSK")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @ManyToOne(targetEntity = Executor.class)
    @JoinColumn(name = "EXECUTOR_ID")
    @ForeignKey(name = "FK_TSKINS_EXECUTOR")
    @Index(name = "IDX_TASK_EXECUTOR")
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public TaskDefinition getTask(ProcessDefinition processDefinition) {
        return processDefinition.getTaskNotNull(name);
    }

    @Transient
    public boolean isStartTask(ExecutionContext executionContext, TaskDefinition taskDefinition) {
        return taskDefinition.equals(executionContext.getProcessDefinition().getStartStateNotNull().getFirstTaskNotNull());
    }

    @Override
    public void assignExecutor(ExecutionContext executionContext, Executor executor, boolean cascadeUpdate) {
        log.debug("assigning task '" + name + "' to '" + executor + "'");
        // log this assignment
        executionContext.addLog(new TaskAssignLog(this, executor));
        // do the actual assignment
        this.executor = executor;
        if (cascadeUpdate) {
            swimlane.assignExecutor(executionContext, executor, false);
        }
        // fire the event
        getTask(executionContext.getProcessDefinition()).fireEvent(new ExecutionContext(executionContext.getProcessDefinition(), this),
                Event.EVENTTYPE_TASK_ASSIGN);
    }

    /**
     * marks this task as done and specifies a transition leaving the task-node for the case that the completion of this tasks triggers a signal on the token. If this task leads to
     * a signal on the token, the given transition name will be used in the signal. If this task completion does not trigger execution to move on, the transition is ignored.
     */
    public void end(ExecutionContext executionContext, Transition transition, boolean leaveNode) {
        Preconditions.checkState(isActive(), "task '" + id + "' is already ended");
        // mark the end of this task
        setEndDate(new Date());
        // fire the task end event
        TaskDefinition taskDefinition = getTask(executionContext.getProcessDefinition());
        taskDefinition.fireEvent(executionContext, Event.EVENTTYPE_TASK_END);
        executionContext.addLog(new TaskEndLog(this));
        // verify if the end of this task triggers continuation of execution
        // ending start tasks always leads to a signal
        if (leaveNode && (isStartTask(executionContext, taskDefinition) || ((TaskNode) taskDefinition.getNode()).isCompletionTriggersSignal(this))) {
            if (transition == null) {
                transition = taskDefinition.getNode().getDefaultLeavingTransitionNotNull();
            }
            log.debug("completion of task '" + name + "' results in taking transition '" + transition + "'");
            token.signal(executionContext, transition);
        }
    }

    @Transient
    public boolean isActive() {
        return endDate == null;
    }

    @Override
    public String toString() {
        return getProcess().getId() + ": " + name;
    }

}

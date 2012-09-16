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
package ru.runa.bpm.taskmgmt.exe;

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

import ru.runa.InternalApplicationException;
import ru.runa.bpm.context.exe.ContextInstance;
import ru.runa.bpm.context.exe.VariableContainer;
import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.node.TaskNode;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.bpm.taskmgmt.log.TaskEndLog;
import ru.runa.commons.SubjectPrincipalHolder;

/**
 * is one task instance that can be assigned to an actor (read: put in someones
 * task list) and that can trigger the coninuation of execution of the token
 * upon completion.
 */
@Entity
@Table(name = "JBPM_TASKINSTANCE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TaskInstance extends VariableContainer implements Assignable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TaskInstance.class);

    private Long id;
    private Long version;
    private String name;
    private String description;
    private String assignedActorId;
    private Date createDate;
    private Date startDate;
    private Date endDate;
    private Date dueDate;
    private boolean open;
    private boolean signalling;
    private Token token;
    private SwimlaneInstance swimlaneInstance;
    private TaskMgmtInstance taskMgmtInstance;
    private ProcessInstance processInstance;

    private Task task;

    public TaskInstance() {
    }

    public TaskInstance(Task task) {
        this.task = task;
        this.name = task.getName();
        this.description = task.getDescription();
        this.signalling = true;
        this.open = true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_TASKINSTANCE")
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

    @Column(name = "DESCRIPTION_")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "CREATE_")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "START_")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Column(name = "END_")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Column(name = "DUEDATE_")
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Column(name = "ISOPEN_")
    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Column(name = "ISSIGNALLING_")
    public boolean isSignalling() {
        return signalling;
    }

    public void setSignalling(boolean signalling) {
        this.signalling = signalling;
    }

    @Override
    @ManyToOne(targetEntity = Token.class)
    @JoinColumn(name = "TOKEN_")
    @ForeignKey(name = "FK_TASKINST_TOKEN")
    @Index(name = "IDX_TASKINST_TOKN")
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @ManyToOne(targetEntity = SwimlaneInstance.class)
    @JoinColumn(name = "SWIMLANINSTANCE_")
    @ForeignKey(name = "FK_TASKINST_SLINST")
    @Index(name = "IDX_TSKINST_SLINST")
    public SwimlaneInstance getSwimlaneInstance() {
        return swimlaneInstance;
    }

    public void setSwimlaneInstance(SwimlaneInstance swimlaneInstance) {
        this.swimlaneInstance = swimlaneInstance;
    }

    @ManyToOne(targetEntity = TaskMgmtInstance.class)
    @JoinColumn(name = "TASKMGMTINSTANCE_")
    @ForeignKey(name = "FK_TASKINST_TMINST")
    @Index(name = "IDX_TSKINST_TMINST")
    public TaskMgmtInstance getTaskMgmtInstance() {
        return taskMgmtInstance;
    }

    public void setTaskMgmtInstance(TaskMgmtInstance taskMgmtInstance) {
        this.taskMgmtInstance = taskMgmtInstance;
    }

    @ManyToOne(targetEntity = ProcessInstance.class)
    @JoinColumn(name = "PROCINST_")
    @ForeignKey(name = "FK_TSKINS_PRCINS")
    @Index(name = "IDX_TASKINST_TSK")
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    @Column(name = "ACTORID_")
    @Index(name = "IDX_TASK_ACTORID")
    public String getAssignedActorId() {
        return assignedActorId;
    }

    public void setAssignedActorId(String assignedActorId) {
        this.assignedActorId = assignedActorId;
    }

    @Transient
    public Task getTask() {
        return task;
    }

    private void submitVariables(ExecutionContext executionContext) {
        if (token != null && token.getProcessInstance() != null) {
            // the default behaviour is that all task-local variables are
            // flushed to the process
            ContextInstance contextInstance = token.getProcessInstance().getContextInstance();
            for (VariableInstance<?> variableInstance : variableInstances.values()) {
                log.debug("flushing variable '" + variableInstance.getName() + "' from task '" + name + "' to process variables");
                // This might be optimized, but this was the simplest way to
                // make a clone of the variable instance.
                contextInstance.setVariable(executionContext, variableInstance.getName(), variableInstance.getValue(), token);
            }
        }
    }

    public void create(ExecutionContext executionContext) {
        if (createDate != null) {
            throw new IllegalStateException("task instance '" + id + "' was already created");
        }
        createDate = new Date();
        // the TASK_CREATE event is fired
        executionContext.setTaskInstance(this);
        task.fireEvent(Event.EVENTTYPE_TASK_CREATE, executionContext);
    }

    public void assign(ExecutionContext executionContext) {
        TaskMgmtInstance taskMgmtInstance = executionContext.getTaskMgmtInstance();
        Swimlane swimlane = task.getSwimlane();

        // if this is a task assignment for a start-state
        if (isStartTaskInstance(executionContext)) {
            // initialize the swimlane
            swimlaneInstance = new SwimlaneInstance(swimlane);
            taskMgmtInstance.addSwimlaneInstance(executionContext, swimlaneInstance);
            // with the current authenticated actor
            // String actorId =
            // JbpmContext.getCurrentJbpmContext().getActorId();
            // TODO SubjectPrincipalHolder ?
            swimlaneInstance.setActorId(executionContext, SubjectPrincipalHolder.actorIds.get());
        } else {
            // lazy initialize the swimlane...
            // get the swimlane instance (if there is any)
            swimlaneInstance = taskMgmtInstance.getInitializedSwimlaneInstance(executionContext, swimlane, task.isReassignSwimlane());
            // copy the swimlaneInstance assignment into the taskInstance
            setSwimlaneInstance(swimlaneInstance);
            setActorId(executionContext, swimlaneInstance.getAssignedActorId());
        }
    }

    @Transient
    private boolean isStartTaskInstance(ExecutionContext executionContext) {
        return task.equals(executionContext.getProcessDefinition().getStartStateNotNull().getFirstTaskNotNull());
    }

    /**
     * (re)assign this task to the given actor. If this task is related to a
     * swimlane instance, that swimlane instance will be updated as well.
     */
    @Override
    public void setActorId(ExecutionContext executionContext, String actorId) {
        setActorId(executionContext, actorId, true);
    }

    /**
     * (re)assign this task to the given actor.
     * 
     * @param actorId
     *            is reference to the person that is assigned to this task.
     * @param overwriteSwimlane
     *            specifies if the related swimlane should be overwritten with
     *            the given swimlaneActorId.
     */
    public void setActorId(ExecutionContext executionContext, String actorId, boolean overwriteSwimlane) {
        // do the actual assignment
        String previousActorId = this.assignedActorId;
        this.assignedActorId = actorId;
        if (swimlaneInstance != null && overwriteSwimlane) {
            log.debug("assigning task '" + name + "' to '" + actorId + "'");
            swimlaneInstance.setActorId(executionContext, actorId);
        }
        // fire the event
        executionContext.setTaskInstance(this);
        // WARNING: The events create and assign are fired in the right
        // order, but
        // the logs are still not ordered properly.
        // See also: TaskMgmtInstance.createTaskInstance
        task.fireEvent(Event.EVENTTYPE_TASK_ASSIGN, executionContext);
        // log this assignment
        token.addLog(new TaskAssignLog(this, previousActorId, actorId));
    }

    private void markAsCancelled() {
        this.open = false;
    }

    /**
     * cancels this task. This task intance will be marked as cancelled and as
     * ended. But cancellation doesn't influence singalling and continuation of
     * process execution.
     */
    public void cancel(ExecutionContext executionContext) {
        markAsCancelled();
        end(executionContext);
    }

    /**
     * cancels this task, takes the specified transition. This task intance will
     * be marked as cancelled and as ended. But cancellation doesn't influence
     * singalling and continuation of process execution.
     */
    public void cancel(ExecutionContext executionContext, String transitionName) {
        markAsCancelled();
        end(executionContext, transitionName);
    }

    /**
     * marks this task as done. If this task is related to a task node this
     * might trigger a signal on the token.
     * 
     * @see #end(Transition)
     */
    public void end(ExecutionContext executionContext) {
        end(executionContext, (Transition) null);
    }

    /**
     * marks this task as done and specifies the name of a transition leaving
     * the task-node for the case that the completion of this task instances
     * triggers a signal on the token. If this task leads to a signal on the
     * token, the given transition name will be used in the signal. If this task
     * completion does not trigger execution to move on, the transitionName is
     * ignored.
     */
    public void end(ExecutionContext executionContext, String transitionName) {
        Transition leavingTransition = task.getNode().getLeavingTransition(transitionName);
        if (leavingTransition == null) {
            throw new InternalApplicationException("task node does not have leaving transition '" + transitionName + "'");
        }
        end(executionContext, leavingTransition);
    }

    /**
     * marks this task as done and specifies a transition leaving the task-node
     * for the case that the completion of this task instances triggers a signal
     * on the token. If this task leads to a signal on the token, the given
     * transition name will be used in the signal. If this task completion does
     * not trigger execution to move on, the transition is ignored.
     */
    public void end(ExecutionContext executionContext, Transition transition) {
        if (this.endDate != null) {
            throw new IllegalStateException("task instance '" + id + "' is already ended");
        }
        // mark the end of this task instance
        this.endDate = new Date();
        this.open = false;
        // fire the task instance end event
        executionContext.setTaskInstance(this);
        task.fireEvent(Event.EVENTTYPE_TASK_END, executionContext);
        token.addLog(new TaskEndLog(this));
        // submit the variables
        submitVariables(executionContext);
        // verify if the end of this task triggers continuation of execution
        if (signalling) {
            this.signalling = false;
            // ending start tasks always leads to a signal
            if (this.isStartTaskInstance(executionContext) || ((TaskNode) task.getNode()).completionTriggersSignal(this)) {
                if (transition == null) {
                    log.debug("completion of task '" + task.getName() + "' results in taking the default transition");
                    token.signal(executionContext);
                } else {
                    log.debug("completion of task '" + task.getName() + "' results in taking transition '" + transition + "'");
                    token.signal(executionContext, transition);
                }
            }
        }
    }

    public boolean hasEnded() {
        return endDate != null;
    }

    @Override
    public String toString() {
        return "TaskInstance" + (name != null ? "(" + name + ")" : "@" + Integer.toHexString(hashCode()));
    }

    @Transient
    @Override
    protected VariableContainer getParentVariableContainer() {
        return getContextInstance().getOrCreateTokenVariableMap(token);
    }

}

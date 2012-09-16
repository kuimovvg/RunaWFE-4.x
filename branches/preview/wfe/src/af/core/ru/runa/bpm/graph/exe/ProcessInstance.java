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
package ru.runa.bpm.graph.exe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

import ru.runa.InternalApplicationException;
import ru.runa.bpm.context.exe.ContextInstance;
import ru.runa.bpm.db.JobDAO;
import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.log.ProcessInstanceCreateLog;
import ru.runa.bpm.graph.log.ProcessInstanceEndLog;
import ru.runa.bpm.job.CleanUpProcessJob;
import ru.runa.bpm.logging.exe.LoggingInstance;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.module.exe.ModuleInstance;
import ru.runa.bpm.taskmgmt.exe.TaskMgmtInstance;
import ru.runa.commons.ApplicationContextFactory;
import ru.runa.commons.EqualsUtil;

/**
 * is one execution of a
 * {@link ru.runa.bpm.graph.def.ExecutableProcessDefinition}.
 */
@Entity
@Table(name = "JBPM_PROCESSINSTANCE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProcessInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private Date startDate;
    private Date endDate;
    private Token rootToken;
    private Token superProcessToken;
    private Map<String, ModuleInstance> instances;
    private String hierarchySubProcess;
    private LoggingInstance loggingInstance;
    private ArchievedProcessDefinition processDefinition;

    /** not persisted TODO remove */
    private List cascadeProcessInstances;

    public ProcessInstance() {
    }

    /**
     * creates a new process instance for the given process definition, puts the
     * root-token (=main path of execution) in the start state and executes the
     * initial node. In case the initial node is a start-state, it will behave
     * as a wait state. For each of the optional module definitions contained in
     * the {@link ArchievedProcessDefinition}, the corresponding module instance
     * will be created.
     * 
     * @throws InternalApplicationException
     *             if processDefinition is null.
     */
    public ProcessInstance(ExecutableProcessDefinition processDefinition) {
        this(processDefinition, null);
    }

    /**
     * creates a new process instance for the given process definition, puts the
     * root-token (=main path of execution) in the start state and executes the
     * initial node. In case the initial node is a start-state, it will behave
     * as a wait state. For each of the optional module definitions contained in
     * the {@link ArchievedProcessDefinition}, the corresponding module instance
     * will be created.
     * 
     * @param variables
     *            will be inserted into the context variables after the context
     *            submodule has been created and before the process-start event
     *            is fired, which is also before the execution of the initial
     *            node.
     * @throws InternalApplicationException
     *             if processDefinition is null.
     */
    public ProcessInstance(ExecutableProcessDefinition processDefinition, Map<String, Object> variables) {
        if (processDefinition == null) {
            throw new InternalApplicationException("can't create a process instance when processDefinition is null");
        }

        // initialize the members
        this.processDefinition = processDefinition.getDBImpl();
        this.rootToken = new Token(processDefinition, this);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, rootToken);

        this.hierarchySubProcess = "" + this.id;

        // create the optional definitions
        // if the state-definition has optional definitions
        instances = new HashMap<String, ModuleInstance>();
        ContextInstance contextInstance = new ContextInstance();
        instances.put(contextInstance.getClass().getName(), contextInstance);
        contextInstance.setProcessInstance(this);

        // add the creation log
        rootToken.addLog(new ProcessInstanceCreateLog());

        // set the variables
        contextInstance.setVariables(executionContext, variables);

        Node initialNode = rootToken.getNode();
        this.startDate = new Date();
        // fire the process start event
        if (initialNode != null) {
            processDefinition.fireEvent(Event.EVENTTYPE_PROCESS_START, executionContext);
            // execute the start node
            initialNode.execute(executionContext);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_PROCESSINSTANCE")
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

    @Column(name = "HIERARCHYSUBPROCESS_")
    public String getHierarchySubProcess() {
        return hierarchySubProcess;
    }

    public void setHierarchySubProcess(String hierarchySubProcess) {
        this.hierarchySubProcess = hierarchySubProcess;
    }

    @Column(name = "START_")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date start) {
        this.startDate = start;
    }

    @Column(name = "END_")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date end) {
        this.endDate = end;
    }

    @OneToMany(fetch = FetchType.EAGER, targetEntity = ModuleInstance.class)
    @JoinColumn(name = "PROCESSINSTANCE_")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @MapKey(targetElement = String.class, columns = @Column(name = "NAME_"))
    public Map<String, ModuleInstance> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, ModuleInstance> instances) {
        this.instances = instances;
    }

    @ManyToOne(targetEntity = ArchievedProcessDefinition.class)
    @JoinColumn(name = "PROCESSDEFINITION_", nullable = false)
    @ForeignKey(name = "FK_PROCIN_PROCDEF")
    @Index(name = "IDX_PROCIN_PROCDEF")
    public ArchievedProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ArchievedProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    @ManyToOne(targetEntity = Token.class, cascade = { javax.persistence.CascadeType.ALL })
    @JoinColumn(name = "ROOTTOKEN_", nullable = false)
    @ForeignKey(name = "FK_PROCIN_ROOTTKN")
    @Index(name = "IDX_PROCIN_ROOTTK")
    public Token getRootToken() {
        return rootToken;
    }

    public void setRootToken(Token rootToken) {
        this.rootToken = rootToken;
    }

    @ManyToOne(targetEntity = Token.class)
    @JoinColumn(name = "SUPERPROCESSTOKEN_")
    @ForeignKey(name = "FK_PROCIN_SPROCTKN")
    @Index(name = "IDX_PROCIN_SPROCTK")
    @Fetch(FetchMode.JOIN)
    public Token getSuperProcessToken() {
        return superProcessToken;
    }

    public void setSuperProcessToken(Token superProcessToken) {
        this.superProcessToken = superProcessToken;
    }

    /**
     * looks up an optional module instance by its class.
     */
    private ModuleInstance getInstance(Class<? extends ModuleInstance> clazz) {
        ModuleInstance moduleInstance = instances.get(clazz.getName());
        if (moduleInstance == null) {
            // client requested an instance that is not in the map of instances.
            // so we can safely assume that the client wants a transient
            // instance
            if (moduleInstance == null) {
                try {
                    moduleInstance = clazz.newInstance();
                    moduleInstance.setProcessInstance(this);
                } catch (Exception e) {
                    throw new InternalApplicationException("couldn't instantiate transient module '" + clazz.getName()
                            + "' with the default constructor");
                }
            }
        }
        return moduleInstance;
    }

    /**
     * process instance extension for process variableInstances.
     */
    @Transient
    public ContextInstance getContextInstance() {
        return (ContextInstance) getInstance(ContextInstance.class);
    }

    /**
     * process instance extension for managing the tasks and actors.
     */
    @Transient
    public TaskMgmtInstance getTaskMgmtInstance() {
        return (TaskMgmtInstance) getInstance(TaskMgmtInstance.class);
    }

    /**
     * process instance extension for logging. Probably you don't need to access
     * the logging instance directly. Mostly, {@link Token#addLog(ProcessLog)}
     * is sufficient and more convenient.
     */
    @Transient
    public LoggingInstance getLoggingInstance() {
        if (loggingInstance == null) {
            loggingInstance = new LoggingInstance();
            loggingInstance.setProcessInstance(this);
        }
        return loggingInstance;
    }

    /**
     * instructs the main path of execution to continue by taking the default
     * transition on the current node.
     * 
     * @throws IllegalStateException
     *             if the token is not active.
     */
    public void signal(ExecutionContext executionContext) {
        if (hasEnded()) {
            throw new IllegalStateException("couldn't signal token : token has ended");
        }
        rootToken.signal(executionContext);
    }

    /**
     * ends (=cancels) this process instance and all the tokens in it.
     */
    public void end(ExecutionContext executionContext) {
        // end the main path of execution
        rootToken.end(executionContext);

        if (endDate == null) {
            // mark this process instance as ended
            endDate = new Date();

            // fire the process-end event
            // OLD stuff ExecutionContext executionContext = new
            // ExecutionContext(rootToken);
            executionContext.getProcessDefinition().fireEvent(Event.EVENTTYPE_PROCESS_END, executionContext);

            // add the process instance end log
            rootToken.addLog(new ProcessInstanceEndLog());

            // check if this process was started as a subprocess of a super
            // process
            if (superProcessToken != null && !superProcessToken.hasEnded()) {
                addCascadeProcessInstance(superProcessToken.getProcessInstance());

                // TODO sub process def
                ExecutionContext superExecutionContext = new ExecutionContext(executionContext.getProcessDefinition(), superProcessToken);
                superExecutionContext.setSubProcessInstance(this);
                superProcessToken.signal(superExecutionContext);
            }

            // make sure all the timers for this process instance are canceled
            // after the process end updates are posted to the
            // database
            // NOTE Only timers should be deleted, messages should be kept.
            CleanUpProcessJob job = new CleanUpProcessJob(this);
            job.setDueDate(new Date());
            JobDAO jobDAO = ApplicationContextFactory.getJobSession();
            jobDAO.saveJob(job, true);
        }
    }

    /**
     * tells if this process instance is still active or not.
     */
    public boolean hasEnded() {
        return (endDate != null);
    }

    /**
     * calculates if this process instance has still options to continue.
     */
    @Transient
    public boolean isTerminatedImplicitly() {
        boolean isTerminatedImplicitly = true;
        if (endDate == null) {
            isTerminatedImplicitly = rootToken.isTerminatedImplicitly();
        }
        return isTerminatedImplicitly;
    }

    /**
     * looks up the token in the tree, specified by the slash-separated token
     * path.
     * 
     * @param tokenPath
     *            is a slash-separated name that specifies a token in the tree.
     * @return the specified token or null if the token is not found.
     */
    public Token findToken(String tokenPath) {
        return (rootToken != null ? rootToken.findToken(tokenPath) : null);
    }

    void addCascadeProcessInstance(ProcessInstance cascadeProcessInstance) {
        if (cascadeProcessInstances == null) {
            cascadeProcessInstances = new ArrayList();
        }
        cascadeProcessInstances.add(cascadeProcessInstance);
    }

    public Collection removeCascadeProcessInstances() {
        Collection removed = cascadeProcessInstances;
        cascadeProcessInstances = null;
        return removed;
    }

    // hack to support comparing hibernate proxies against the real objects
    // since this always falls back to ==, we don't need to overwrite the
    // hashcode
    @Override
    public boolean equals(Object o) {
        return EqualsUtil.equals(this, o); // TODO ?
    }

    @Override
    public String toString() {
        return "ProcessInstance" + " (id: " + this.id + ") @" + Integer.toHexString(hashCode());
    }

}

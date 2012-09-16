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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKey;
import org.hibernate.criterion.Expression;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.log.SignalLog;
import ru.runa.bpm.graph.log.TokenCreateLog;
import ru.runa.bpm.graph.log.TokenEndLog;
import ru.runa.bpm.logging.log.CompositeLog;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.exe.TaskMgmtInstance;
import ru.runa.commons.ApplicationContextFactory;
import ru.runa.commons.EqualsUtil;

/**
 * represents one path of execution and maintains a pointer to a node in the
 * {@link ru.runa.bpm.graph.def.ExecutableProcessDefinition}. Most common way to
 * get a hold of the token objects is with
 * {@link ProcessInstance#getRootToken()} or
 * {@link ru.runa.bpm.graph.exe.ProcessInstance#findToken(String)}.
 */
@Entity
@Table(name = "JBPM_TOKEN")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Token implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long version;
    private String name;
    private Date startDate;
    private Date endDate;
    private Date nodeEnterDate;
    private ProcessInstance processInstance;
    private Token parent;
    private Map<String, Token> children;
    private ProcessInstance subProcessInstance;
    private Set<ProcessInstance> subProcessMultiInstance;
    private int nextLogIndex;
    private boolean ableToReactivateParent;

    private Node node;

    public Token() {
    }

    /**
     * creates a root token.
     */
    public Token(ExecutableProcessDefinition processDefinition, ProcessInstance processInstance) {
        this.startDate = new Date();
        this.processInstance = processInstance;
        this.node = processDefinition.getStartStateNotNull();
    }

    /**
     * creates a child token.
     */
    public Token(Token parent, String name) {
        this.startDate = new Date();
        this.processInstance = parent.getProcessInstance();
        this.name = name;
        this.node = parent.getNode();
        this.parent = parent;
        parent.addChild(this);
        parent.addLog(new TokenCreateLog(this));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_JBPM_TOKEN")
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

    @Column(name = "NODEENTER_")
    public Date getNodeEnterDate() {
        return nodeEnterDate;
    }

    public void setNodeEnterDate(Date nodeEnter) {
        this.nodeEnterDate = nodeEnter;
    }

    @Column(name = "NEXTLOGINDEX_")
    public int getNextLogIndex() {
        return nextLogIndex;
    }

    public void setNextLogIndex(int nextLogIndex) {
        this.nextLogIndex = nextLogIndex;
    }

    @Transient
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @ManyToOne(targetEntity = ProcessInstance.class)
    @JoinColumn(name = "PROCESSINSTANCE_"/* , nullable = false */)
    @ForeignKey(name = "FK_TOKEN_PROCINST")
    @Index(name = "IDX_TOKEN_PROCIN")
    @Fetch(FetchMode.JOIN)
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    @ManyToOne(targetEntity = Token.class)
    @JoinColumn(name = "PARENT_"/* , nullable = false */)
    @ForeignKey(name = "FK_TOKEN_PARENT")
    @Index(name = "IDX_TOKEN_PARENT")
    @Fetch(FetchMode.JOIN)
    public Token getParent() {
        return parent;
    }

    public void setParent(Token parent) {
        this.parent = parent;
    }

    @ManyToOne(targetEntity = ProcessInstance.class)
    @JoinColumn(name = "SUBPROCESSINSTANCE_")
    @ForeignKey(name = "FK_TOKEN_SUBPI")
    @Index(name = "IDX_TOKEN_SUBPI")
    @Fetch(FetchMode.JOIN)
    public ProcessInstance getSubProcessInstance() {
        return subProcessInstance;
    }

    public void setSubProcessInstance(ProcessInstance subProcessInstance) {
        this.subProcessInstance = subProcessInstance;
    }

    @OneToMany(fetch = FetchType.EAGER, targetEntity = Token.class)
    @JoinColumn(name = "PARENT_")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @MapKey(targetElement = String.class, columns = @Column(name = "NAME_"))
    public Map<String, Token> getChildren() {
        return children;
    }

    public void setChildren(Map<String, Token> children) {
        this.children = children;
    }

    @OneToMany(fetch = FetchType.EAGER, targetEntity = ProcessInstance.class)
    @JoinColumn(name = "SUPERPROCESSTOKEN_")
    public Set<ProcessInstance> getSubProcessMultiInstance() {
        return subProcessMultiInstance;
    }

    public void setSubProcessMultiInstance(Set<ProcessInstance> subProcessMultiInstance) {
        this.subProcessMultiInstance = subProcessMultiInstance;
    }

    @Column(name = "ISABLETOREACTIVATEPARENT_")
    public boolean isAbleToReactivateParent() {
        return ableToReactivateParent;
    }

    public void setAbleToReactivateParent(boolean ableToReactivateParent) {
        this.ableToReactivateParent = ableToReactivateParent;
    }

    private void addChild(Token token) {
        if (children == null) {
            children = new HashMap<String, Token>();
        }
        children.put(token.getName(), token);
    }

    public void signal(ExecutionContext executionContext) {
        signal(executionContext, node.getDefaultLeavingTransition());
    }

    public void signal(ExecutionContext executionContext, Transition transition) {
        if (node == null) {
            throw new InternalApplicationException("token '" + this + "' can't be signalled cause it is currently not positioned in a node");
        }
        if (node.getDefaultLeavingTransition() == null) {
            throw new InternalApplicationException("couldn't signal token '" + this + "' : node '" + node + "' doesn't have a default transition");
        }
        if (transition == null) {
            throw new InternalApplicationException("couldn't signal without specifying  a leaving transition : transition is null");
        }
        if (executionContext == null) {
            throw new InternalApplicationException("couldn't signal without an execution context: executionContext is null");
        }
        startCompositeLog(new SignalLog(transition));
        try {
            // fire the event before-signal
            node.fireEvent(Event.EVENTTYPE_BEFORE_SIGNAL, executionContext);
            // start calculating the next state
            node.leave(executionContext, transition);
            // fire the event after-signal
            node.fireEvent(Event.EVENTTYPE_AFTER_SIGNAL, executionContext);
        } finally {
            endCompositeLog();
        }
    }

    /**
     * ends this token and all of its children (if any). this is the last active
     * (=not-ended) child of a parent token, the parent token will be ended as
     * well and that verification will continue to propagate.
     */
    public void end(ExecutionContext executionContext) {
        end(executionContext, true);
    }

    /**
     * ends this token with optional parent ending verification.
     * 
     * @param verifyParentTermination
     *            specifies if the parent token should be checked for
     *            termination. if verifyParentTermination is set to true and
     *            this is the last non-ended child of a parent token, the parent
     *            token will be ended as well and the verification will continue
     *            to propagate.
     */
    public void end(ExecutionContext executionContext, boolean verifyParentTermination) {
        // if not already ended
        if (endDate == null) {
            // ended tokens cannot reactivate parents
            ableToReactivateParent = false;
            // set the end date
            // the end date is also the flag that indicates that this token has
            // ended.
            this.endDate = new Date();
            // end all this token's children
            if (children != null) {
                for (Token child : children.values()) {
                    if (!child.hasEnded()) {
                        child.end(executionContext);
                    }
                }
            }
            if (subProcessInstance != null) {
                subProcessInstance.end(executionContext);
            }
            if (subProcessMultiInstance != null) {
                for (ProcessInstance subInstance : subProcessMultiInstance) {
                    subInstance.end(executionContext);
                }
            }
            // only log the end of child-tokens. the process instance logs
            // replace the root token logs.
            if (parent != null) {
                // add a log
                parent.addLog(new TokenEndLog(this));
            }
            // if there are tasks associated to this token, remove signaling
            // capabilities
            TaskMgmtInstance taskMgmtInstance = (processInstance != null ? processInstance.getTaskMgmtInstance() : null);
            if (taskMgmtInstance != null) {
                taskMgmtInstance.removeSignalling(this);
            }
            if (verifyParentTermination) {
                // if this is the last active token of the parent,
                // the parent needs to be ended as well
                notifyParentOfTokenEnd(executionContext);
            }
        }
    }

    // operations helper methods
    // ////////////////////////////////////////////////
    /**
     * notifies a parent that one of its nodeMap has ended.
     */
    private void notifyParentOfTokenEnd(ExecutionContext executionContext) {
        if (isRoot()) {
            processInstance.end(executionContext);
        } else {
            if (!parent.hasActiveChildren()) {
                parent.end(executionContext);
            }
        }
    }

    /**
     * tells if this token has child tokens that have not yet ended.
     */
    public boolean hasActiveChildren() {
        boolean foundActiveChildToken = false;
        // try and find at least one child token that is
        // still active (= not ended)
        if (children != null && !foundActiveChildToken) {
            for (Token child : children.values()) {
                if (!child.hasEnded()) {
                    foundActiveChildToken = true;
                    break;
                }
            }
        }
        return foundActiveChildToken;
    }

    // log convenience methods
    // //////////////////////////////////////////////////
    /**
     * convenience method for adding a process log.
     */
    public void addLog(ProcessLog processLog) {
        processLog.setToken(this);
        processInstance.getLoggingInstance().addLog(processLog);
    }

    /**
     * convenience method for starting a composite log. When you add composite
     * logs, make sure you put the {@link #endCompositeLog()} in a finally
     * block.
     */
    public void startCompositeLog(CompositeLog compositeLog) {
        compositeLog.setToken(this);
        processInstance.getLoggingInstance().startCompositeLog(compositeLog);
    }

    /**
     * convenience method for ending a composite log. Make sure you put this in
     * a finally block.
     */
    public void endCompositeLog() {
        processInstance.getLoggingInstance().endCompositeLog();
    }

    // various information extraction methods
    // ///////////////////////////////////
    @Override
    public String toString() {
        return "Token(" + getFullName() + ")";
    }

    public boolean hasEnded() {
        return endDate != null;
    }

    @Transient
    public boolean isRoot() {
        return parent == null;
    }

    public boolean hasChild(String name) {
        return children != null ? children.containsKey(name) : false;
    }

    public Token getChild(String name) {
        Token child = null;
        if (children != null) {
            child = children.get(name);
        }
        return child;
    }

    @Transient
    public String getFullName() {
        if (parent == null) {
            return "/";
        }
        if (parent.getParent() == null) {
            return "/" + name;
        }
        return parent.getFullName() + "/" + name;
    }

    public Token findToken(String relativeTokenPath) {
        if (relativeTokenPath == null) {
            return null;
        }
        String path = relativeTokenPath.trim();
        if (("".equals(path)) || (".".equals(path))) {
            return this;
        }
        if ("..".equals(path)) {
            return parent;
        }
        if (path.startsWith("/")) {
            Token root = processInstance.getRootToken();
            return root.findToken(path.substring(1));
        }
        if (path.startsWith("./")) {
            return findToken(path.substring(2));
        }
        if (path.startsWith("../")) {
            if (parent != null) {
                return parent.findToken(path.substring(3));
            }
            return null;
        }
        int slashIndex = path.indexOf('/');
        if (slashIndex == -1) {
            return (children != null ? children.get(path) : null);
        }
        Token token = null;
        String name = path.substring(0, slashIndex);
        token = children.get(name);
        if (token != null) {
            return token.findToken(path.substring(slashIndex + 1));
        }
        return null;
    }

    @Transient
    public Map<String, Token> getActiveChildren() {
        Map<String, Token> activeChildren = new HashMap<String, Token>();
        if (children != null) {
            for (String childName : children.keySet()) {
                Token child = children.get(childName);
                if (!child.hasEnded()) {
                    activeChildren.put(childName, child);
                }
            }
        }
        return activeChildren;
    }

    // TODO: token death example
    // public void checkImplicitTermination() {
    // if (terminationImplicit && node.hasNoLeavingTransitions()) {
    // end();
    // if (processInstance.isTerminatedImplicitly()) {
    // processInstance.end();
    // }
    // }
    // }

    @Transient
    public boolean isTerminatedImplicitly() {
        if (endDate != null) {
            return true;
        }
        Map<String, Transition> leavingTransitions = node.getLeavingTransitionsMap();
        if ((leavingTransitions != null) && (leavingTransitions.size() > 0)) {
            // ok: found a non-terminated token
            return false;
        }
        // loop over all active child tokens
        for (Token child : getActiveChildren().values()) {
            if (!child.isTerminatedImplicitly()) {
                return false;
            }
        }
        // if none of the above, this token is terminated implicitly
        return true;
    }

    public int nextLogIndex() {
        return nextLogIndex++;
    }

    // TODO
    // hack to support comparing hibernate proxies against the real objects
    // since this always falls back to ==, we don't need to overwrite the
    // hashcode
    @Override
    public boolean equals(Object o) {
        return EqualsUtil.equals(this, o);
    }

    public ProcessInstance createSubProcessInstance(ExecutableProcessDefinition subProcessDefinition) {
        // create the new sub process instance
        subProcessInstance = new ProcessInstance(subProcessDefinition);
        // bind the subprocess to the super-process-token
        setSubProcessInstance(subProcessInstance);
        subProcessInstance.setSuperProcessToken(this);
        // make sure the process gets saved during super process save
        subProcessInstance.setHierarchySubProcess(this.getProcessInstance().getHierarchySubProcess() + "/" + subProcessInstance.getId());
        processInstance.addCascadeProcessInstance(subProcessInstance);
        return subProcessInstance;
    }

    public Set<ProcessInstance> getSubProcessMultiInstance(Node node) {
        Session session = ApplicationContextFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(StartedSubprocesses.class);
        criteria.add(Expression.eq("node", node));
        criteria.add(Expression.eq("processInstance", this.processInstance));
        List<StartedSubprocesses> subprocesses = criteria.list();
        Set<ProcessInstance> result = new HashSet<ProcessInstance>();
        for (StartedSubprocesses sub : subprocesses) {
            result.add(sub.getSubProcessInstance());
        }
        return result;
    }
}

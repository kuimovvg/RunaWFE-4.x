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
package ru.runa.wfe.lang;

import java.util.List;
import java.util.Set;

import ru.runa.wfe.ApplicationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class Node extends GraphElement {
    private static final long serialVersionUID = 1L;
    private static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE,
            Event.EVENTTYPE_BEFORE_SIGNAL, Event.EVENTTYPE_AFTER_SIGNAL };

    private final List<Transition> leavingTransitions = Lists.newArrayList();
    private final Set<Transition> arrivingTransitions = Sets.newHashSet();

    public abstract NodeType getNodeType();

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    @Override
    public void validate() {
        super.validate();
        for (Transition transition : leavingTransitions) {
            transition.validate();
        }
    }

    public List<Transition> getLeavingTransitions() {
        return leavingTransitions;
    }

    /**
     * Arriving transitions for node.
     */
    public Set<Transition> getArrivingTransitions() {
        return arrivingTransitions;
    }

    /**
     * creates a bidirection relation between this node and the given leaving
     * transition.
     * 
     * @throws IllegalArgumentException
     *             if leavingTransition is null.
     */
    public Transition addLeavingTransition(Transition leavingTransition) {
        leavingTransitions.add(leavingTransition);
        leavingTransition.setFrom(this);
        return leavingTransition;
    }

    /**
     * checks for the presence of a leaving transition with the given name.
     * 
     * @return true if this node has a leaving transition with the given name,
     *         false otherwise.
     */
    public boolean hasLeavingTransition(String transitionName) {
        return getLeavingTransition(transitionName) != null;
    }

    /**
     * retrieves a leaving transition by name. note that also the leaving
     * transitions of the supernode are taken into account.
     */
    public Transition getLeavingTransition(String transitionName) {
        for (Transition transition : leavingTransitions) {
            if (transitionName.equals(transition.getName())) {
                return transition;
            }
        }
        return null;
    }

    public Transition getLeavingTransitionNotNull(String transitionName) {
        Transition transition = getLeavingTransition(transitionName);
        if (transition == null) {
            throw new ApplicationException("leaving transition '" + transitionName + "' does not exist in " + this);
        }
        return transition;
    }

    /**
     * @return the default leaving transition.
     */
    public Transition getDefaultLeavingTransitionNotNull() {
        for (Transition transition : leavingTransitions) {
            if (!Transition.TIMEOUT_TRANSITION_NAME.equals(transition.getName())) {
                return transition;
            }
        }
        if (leavingTransitions.size() > 0) {
            return leavingTransitions.get(0);
        }
        throw new InternalApplicationException("No leaving transitions in " + this);
    }

    /**
     * add a bidirection relation between this node and the given arriving
     * transition.
     * 
     * @throws IllegalArgumentException
     *             if t is null.
     */
    public Transition addArrivingTransition(Transition arrivingTransition) {
        arrivingTransitions.add(arrivingTransition);
        arrivingTransition.setTo(this);
        return arrivingTransition;
    }

    /**
     * called by a transition to pass execution to this node.
     */
    public final void enter(ExecutionContext executionContext) {
        Token token = executionContext.getToken();
        // update the runtime context information
        token.setNodeId(getNodeId());
        token.setNodeType(getNodeType());
        // fire the leave-node event for this node
        fireEvent(executionContext, Event.EVENTTYPE_NODE_ENTER);
        executionContext.addLog(new NodeEnterLog(this));
        execute(executionContext);
    }

    /**
     * override this method to customize the node behaviour.
     */
    protected abstract void execute(ExecutionContext executionContext);

    /**
     * called by the implementation of this node to continue execution over the
     * default transition.
     */
    public final void leave(ExecutionContext executionContext) {
        leave(executionContext, null);
    }

    /**
     * called by the implementation of this node to continue execution over the
     * given transition.
     */
    public void leave(ExecutionContext executionContext, Transition transition) {
        Token token = executionContext.getToken();
        // fire the leave-node event for this node
        fireEvent(executionContext, Event.EVENTTYPE_NODE_LEAVE);
        // log this node
        executionContext.addLog(new NodeLeaveLog(this));
        if (transition == null) {
            transition = getDefaultLeavingTransitionNotNull();
        }
        token.setNodeId(null);
        token.setNodeType(null);
        // take the transition
        transition.take(executionContext);
    }

}

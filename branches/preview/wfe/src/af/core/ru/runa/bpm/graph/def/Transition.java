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
package ru.runa.bpm.graph.def;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.PassedTransition;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.log.TransitionLog;
import ru.runa.commons.ApplicationContextFactory;

public class Transition extends GraphElement {
    private static final long serialVersionUID = 1L;

    private Node from;
    private Node to;

    // event types
    // //////////////////////////////////////////////////////////////

    public static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_TRANSITION };

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    public Node getFrom() {
        return from;
    }

    /*
     * sets the from node unidirectionally. use {@link
     * Node#addLeavingTransition(Transition)} to get bidirectional relations
     * mgmt.
     */
    public void setFrom(Node from) {
        this.from = from;
    }

    // to
    // ///////////////////////////////////////////////////////////////////////

    /*
     * sets the to node unidirectionally. use {@link
     * Node#addArrivingTransition(Transition)} to get bidirectional relations
     * mgmt.
     */
    public void setTo(Node to) {
        this.to = to;
    }

    public Node getTo() {
        return to;
    }

    // behaviour
    // ////////////////////////////////////////////////////////////////

    /*
     * passes execution over this transition.
     */
    public void take(ExecutionContext executionContext) {
        // update the runtime context information
        executionContext.getToken().setNode(null);

        Token token = executionContext.getToken();

        // if ((condition != null) && (isConditionEnforced)) {
        // Object result = JbpmExpressionEvaluator.evaluate(condition,
        // executionContext);
        // if (result == null) {
        // throw new InternalApplicationException("transition condition " +
        // condition + " evaluated to null");
        // } else if (!(result instanceof Boolean)) {
        // throw new InternalApplicationException("transition condition " +
        // condition + " evaluated to non-boolean: " +
        // result.getClass().getName());
        // } else if (!((Boolean) result).booleanValue()) {
        // throw new InternalApplicationException("transition condition " +
        // condition + " evaluated to 'false'");
        // }
        // }

        // start the transition log
        TransitionLog transitionLog = new TransitionLog(this, executionContext.getTransitionSource());
        token.startCompositeLog(transitionLog);
        try {
            // fire the transition event (if any)
            fireEvent(Event.EVENTTYPE_TRANSITION, executionContext);

            // fire enter events for superstates (if any)
            Node destination = fireSuperStateEnterEvents(executionContext);
            // update the ultimate destinationNode of this transition
            transitionLog.setDestinationNode(destination);

        } finally {
            // end the transition log
            token.endCompositeLog();
        }

        // pass the token to the destinationNode node
        to.enter(executionContext);
    }

    Node fireSuperStateEnterEvents(ExecutionContext executionContext) {
        // calculate the actual destinationNode node
        Node destination = to;
        while (destination != null && destination.isSuperStateNode()) {
            List nodes = destination.getNodes();
            destination = nodes != null && !nodes.isEmpty() ? (Node) nodes.get(0) : null;
        }

        if (destination == null) {
            String transitionName = (name != null ? "'" + name + "'" : "in node '" + from + "'");
            throw new InternalApplicationException("transition " + transitionName + " doesn't have destination. check your processdefinition.xml");
        }

        ApplicationContextFactory.getCurrentSession().save(new PassedTransition(executionContext.getProcessInstance(), this));
        return destination;
    }

    // other
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void setName(String name) {
        if (from != null) {
            if (from.hasLeavingTransition(name)) {
                throw new IllegalArgumentException("couldn't set name '" + name + "' on transition '" + this
                        + "'cause the from-node of this transition has already another leaving transition with the same name");
            }
            Map fromLeavingTransitions = from.getLeavingTransitionsMap();
            fromLeavingTransitions.remove(this.name);
            fromLeavingTransitions.put(name, this);
        }
        this.name = name;
    }

    @Override
    public GraphElement getParent() {
        GraphElement parent = null;
        if ((from != null) && (to != null)) {
            if (from.equals(to)) {
                parent = from.getParent();
            } else {
                List fromParentChain = from.getParentChain();
                List toParentChain = to.getParentChain();
                Iterator fromIter = fromParentChain.iterator();
                while (fromIter.hasNext() && (parent == null)) {
                    GraphElement fromParent = (GraphElement) fromIter.next();
                    Iterator toIter = toParentChain.iterator();
                    while (toIter.hasNext() && (parent == null)) {
                        GraphElement toParent = (GraphElement) toIter.next();
                        if (fromParent == toParent) {
                            parent = fromParent;
                        }
                    }
                }
            }
        }
        return parent;
    }
}

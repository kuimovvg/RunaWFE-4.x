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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.log.ActionLog;
import ru.runa.commons.EqualsUtil;

import com.google.common.base.Throwables;

public abstract class GraphElement implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(GraphElement.class);

    private String id;
    protected String name;
    protected String description;
    protected ExecutableProcessDefinition processDefinition;
    protected Map<String, Event> events;

    public String getNodeId() {
        return id;
    }

    /**
     * indicative set of event types supported by this graph element. this is
     * currently only used by the process designer to know which event types to
     * show on a given graph element. in process definitions and at runtime,
     * there are no contstraints on the event-types.
     */
    public abstract String[] getSupportedEventTypes();

    public Map<String, Event> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }

    public Event getEvent(String eventType) {
        Event event = null;
        if (events != null) {
            event = events.get(eventType);
        }
        return event;
    }

    public boolean hasEvent(String eventType) {
        boolean hasEvent = false;
        if (events != null) {
            hasEvent = events.containsKey(eventType);
        }
        return hasEvent;
    }

    public Event addEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("can't add null event to graph element");
        }
        if (event.getEventType() == null) {
            throw new IllegalArgumentException("can't add an event without type to graph element");
        }
        if (events == null) {
            events = new HashMap<String, Event>();
        }
        events.put(event.getEventType(), event);
        event.setGraphElement(this);
        return event;
    }

    // event handling
    // ///////////////////////////////////////////////////////////

    public void fireEvent(String eventType, ExecutionContext executionContext) {
        log.debug("event '" + eventType + "' on '" + this + "' for '" + executionContext.getToken() + "'");
        fireAndPropagateEvent(eventType, executionContext);
    }

    public void fireAndPropagateEvent(String eventType, ExecutionContext executionContext) {
        // calculate if the event was fired on this element or if it was a
        // propagated event
        // boolean isPropagated =
        // !(this.equals(executionContext.getEventSource()));

        // execute static actions
        Event event = getEvent(eventType);
        if (event != null) {
            // execute the static actions specified in the process definition
            executeActions(event.getActions(), executionContext, false);
        }

        // propagate the event to the parent element
        GraphElement parent = getParent();
        if (parent != null) {
            parent.fireAndPropagateEvent(eventType, executionContext);
        }
    }

    protected void executeActions(List<Action> actions, ExecutionContext executionContext, boolean propagated) {
        if (actions != null) {
            for (Action action : actions) {
                if (action.isPropagationAllowed() || !propagated) {
                    executeAction(action, executionContext);
                }
            }
        }
    }

    public void executeAction(Action action, ExecutionContext executionContext) {
        Token token = executionContext.getToken();

        // create action log
        ActionLog actionLog = new ActionLog(action);
        token.startCompositeLog(actionLog);

        try {
            // execute the action
            log.debug("executing action '" + action + "'");
            action.execute(executionContext);
        } catch (Exception exception) {
            // NOTE that Errors are not caught because that might halt the JVM
            // and mask the original Error
            log.error("action threw exception: " + exception.getMessage(), exception);

            // if an exception handler is available
            raiseException(exception, executionContext);
        } finally {
            token.endCompositeLog();
        }
    }

    /**
     * throws an ActionException if no applicable exception handler is found. An
     * ExceptionHandler is searched for in this graph element and then
     * recursively up the parent hierarchy. If an exception handler is found, it
     * is applied. If the exception handler does not throw an exception, the
     * exception is considered handled. Otherwise the search for an applicable
     * exception handler continues where it left of with the newly thrown
     * exception.
     */
    public void raiseException(Throwable exception, ExecutionContext executionContext) throws DelegationException {
        // there was exceptionHandlers
        GraphElement parent = getParent();
        // if this graph element has a parent
        if (parent != null && !equals(parent)) {
            // raise to the parent
            parent.raiseException(exception, executionContext);
            return;
        }
        // if there is no parent we need to throw a delegation exception to the
        // client
        Throwables.propagateIfInstanceOf(exception, InternalApplicationException.class);
        throw new DelegationException(exception, executionContext);
    }

    public GraphElement getParent() {
        return processDefinition;
    }

    /**
     * @return this graph element plus all the parents ordered by age.
     */
    public List<GraphElement> getParentChain() {
        List<GraphElement> parents = new ArrayList<GraphElement>();
        this.addParentChain(parents);
        return parents;
    }

    void addParentChain(List<GraphElement> parentChain) {
        parentChain.add(this);
        GraphElement parent = getParent();
        if (parent != null) {
            parent.addParentChain(parentChain);
        }
    }

    @Override
    public String toString() {
        String className = getClass().getName();
        className = className.substring(className.lastIndexOf('.') + 1);
        if (name != null) {
            className = className + "(" + name + ")";
        } else {
            className = className + "(" + Integer.toHexString(System.identityHashCode(this)) + ")";
        }
        return className;
    }

    // TODO equals
    // hack to support comparing hibernate proxies against the real objects
    // since this always falls back to ==, we don't need to overwrite the
    // hashcode
    @Override
    public boolean equals(Object o) {
        return EqualsUtil.equals(this, o);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExecutableProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ExecutableProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

}

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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.execution.ExecutionContext;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public abstract class GraphElement implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(GraphElement.class);

    private String nodeId;
    protected String name;
    protected String description;
    protected ProcessDefinition processDefinition;
    protected Map<String, Event> events = Maps.newHashMap();

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (getNodeId() == null) {
            setNodeId(name);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    /**
     * indicative set of event types supported by this graph element. this is
     * currently only used by the process designer to know which event types to
     * show on a given graph element. in process definitions and at runtime,
     * there are no constraints on the event-types.
     */
    public abstract String[] getSupportedEventTypes();

    /**
     * Checks all prerequisites needed for execution.
     */
    public void validate() {
        Preconditions.checkNotNull(nodeId, "id");
        Preconditions.checkNotNull(name, "name");
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public Event getEvent(String eventType) {
        return events.get(eventType);
    }

    public Event addEvent(Event event) {
        Preconditions.checkArgument(event != null, "can't add null event to graph element");
        Preconditions.checkArgument(event.getEventType() != null, "can't add an event without type to graph element");
        events.put(event.getEventType(), event);
        return event;
    }

    public Action getAction(String id) {
        for (Entry<String, Event> entry : getEvents().entrySet()) {
            for (Action action : entry.getValue().getActions()) {
                if (id.equals(action.getName())) {
                    return action;
                }
            }
        }
        return null;
    }

    public void fireEvent(ExecutionContext executionContext, String eventType) {
        log.debug("event '" + eventType + "' on '" + this + "' for '" + executionContext.getToken() + "'");
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
            parent.fireEvent(executionContext, eventType);
        }
    }

    protected void executeActions(List<Action> actions, ExecutionContext executionContext, boolean propagated) {
        for (Action action : actions) {
            if (action.isPropagationAllowed() || !propagated) {
                executeAction(action, executionContext);
            }
        }
    }

    public void executeAction(Action action, ExecutionContext executionContext) {
        try {
            executionContext.addLog(new ActionLog(action));
            // execute the action
            log.debug("executing action '" + action + "'");
            action.execute(executionContext);
        } catch (Exception exception) {
            Throwables.propagateIfInstanceOf(exception, RuntimeException.class);
            throw new InternalApplicationException(exception);
        }
    }

    public GraphElement getParent() {
        return processDefinition;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", getNodeId()).add("name", getName()).toString();
    }

}

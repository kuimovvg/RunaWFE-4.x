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
package ru.runa.bpm.taskmgmt.def;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.GraphElement;
import ru.runa.bpm.graph.node.InteractionNode;

/**
 * defines a task and how the actor must be calculated at runtime.
 */
public class Task extends GraphElement {
    public static final int PRIORITY_HIGHEST = 1;
    public static final int PRIORITY_HIGH = 2;
    public static final int PRIORITY_NORMAL = 3;
    public static final int PRIORITY_LOW = 4;
    public static final int PRIORITY_LOWEST = 5;

    public static int parsePriority(String priorityText) {
        if ("highest".equalsIgnoreCase(priorityText))
            return PRIORITY_HIGHEST;
        else if ("high".equalsIgnoreCase(priorityText))
            return PRIORITY_HIGH;
        else if ("normal".equalsIgnoreCase(priorityText))
            return PRIORITY_NORMAL;
        else if ("low".equalsIgnoreCase(priorityText))
            return PRIORITY_LOW;
        else if ("lowest".equalsIgnoreCase(priorityText))
            return PRIORITY_LOWEST;
        try {
            return Integer.parseInt(priorityText);
        } catch (NumberFormatException e) {
            throw new InternalApplicationException("priority '" + priorityText + "' could not be parsed as a priority");
        }
    }

    protected int priority = PRIORITY_NORMAL;
    protected String dueDate;
    protected InteractionNode node;
    protected Swimlane swimlane;
    protected boolean reassignSwimlane;

    public Task() {
    }

    static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_TASK_CREATE, Event.EVENTTYPE_TASK_ASSIGN, Event.EVENTTYPE_TASK_START,
            Event.EVENTTYPE_TASK_END };

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    /**
     * sets the swimlane unidirectionally.  Since a task can have max one of swimlane or assignmentHandler, 
     * this method removes the assignmentHandler and assignmentExpression if one of those isset.
     */
    public void setSwimlane(Swimlane swimlane) {
        this.swimlane = swimlane;
    }

    public void setReassignSwimlane(boolean reassignSwimlane) {
        this.reassignSwimlane = reassignSwimlane;
    }

    @Override
    public GraphElement getParent() {
        return node;
    }

    public Swimlane getSwimlane() {
        return swimlane;
    }

    public boolean isReassignSwimlane() {
        return reassignSwimlane;
    }

    public InteractionNode getNode() {
        return node;
    }
    public void setNode(InteractionNode node) {
        this.node = node;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String duedate) {
        this.dueDate = duedate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}

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

import com.google.common.base.Preconditions;

/**
 * defines a task and how the actor must be calculated at runtime.
 */
public class TaskDefinition extends GraphElement {
    private static final long serialVersionUID = 1L;

    private static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_TASK_CREATE, Event.EVENTTYPE_TASK_ASSIGN,
            Event.EVENTTYPE_TASK_START, Event.EVENTTYPE_TASK_END };

    protected String deadlineDuration;
    protected InteractionNode node;
    protected SwimlaneDefinition swimlaneDefinition;
    protected boolean reassignSwimlane;

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(node, "node");
        if (!(node instanceof MultiTaskNode)) {
            Preconditions.checkNotNull(swimlaneDefinition, "swimlane");
        }
    }

    /**
     * sets the swimlane unidirectionally. Since a task can have max one of
     * swimlane or assignmentHandler, this method removes the assignmentHandler
     * and assignmentExpression if one of those isset.
     */
    public void setSwimlane(SwimlaneDefinition swimlaneDefinition) {
        this.swimlaneDefinition = swimlaneDefinition;
    }

    public void setReassignSwimlane(boolean reassignSwimlane) {
        this.reassignSwimlane = reassignSwimlane;
    }

    @Override
    public GraphElement getParent() {
        return node;
    }

    public SwimlaneDefinition getSwimlane() {
        return swimlaneDefinition;
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

    public String getDeadlineDuration() {
        return deadlineDuration;
    }

    public void setDeadlineDuration(String deadlineDuration) {
        this.deadlineDuration = deadlineDuration;
    }

}

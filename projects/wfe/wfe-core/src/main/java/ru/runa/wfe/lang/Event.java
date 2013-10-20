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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String EVENTTYPE_TRANSITION = "transition";
    public static final String EVENTTYPE_BEFORE_SIGNAL = "before-signal";
    public static final String EVENTTYPE_AFTER_SIGNAL = "after-signal";
    public static final String EVENTTYPE_PROCESS_START = "process-start";
    public static final String EVENTTYPE_PROCESS_END = "process-end";
    public static final String EVENTTYPE_NODE_ENTER = "node-enter";
    public static final String EVENTTYPE_NODE_LEAVE = "node-leave";
    public static final String EVENTTYPE_SUPERSTATE_ENTER = "superstate-enter";
    public static final String EVENTTYPE_SUPERSTATE_LEAVE = "superstate-leave";
    // public static final String EVENTTYPE_SUBPROCESS_CREATED =
    // "subprocess-created";
    public static final String EVENTTYPE_SUBPROCESS_START = "subprocess-start";
    public static final String EVENTTYPE_SUBPROCESS_END = "subprocess-end";
    public static final String EVENTTYPE_TASK_CREATE = "task-create";
    public static final String EVENTTYPE_TASK_ASSIGN = "task-assign";
    public static final String EVENTTYPE_TASK_START = "task-start";
    public static final String EVENTTYPE_TASK_END = "task-end";
    public static final String EVENTTYPE_TIMER_CREATE = "timer-create";
    public static final String EVENTTYPE_TIMER = "timer";

    private final String eventType;
    private final List<Action> actions = Lists.newArrayList();

    public Event(String eventType) {
        this.eventType = eventType;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Action addAction(Action action) {
        Preconditions.checkNotNull(action, "can't add a null action to an event");
        actions.add(action);
        action.setEvent(this);
        return action;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return eventType;
    }
}

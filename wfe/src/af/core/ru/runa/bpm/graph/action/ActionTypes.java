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
package ru.runa.bpm.graph.action;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.node.NodeTypes;
import ru.runa.bpm.scheduler.def.CancelTimerAction;
import ru.runa.bpm.scheduler.def.CreateTimerAction;

import com.google.common.collect.Maps;

public class ActionTypes {

    public static Set getActionTypes() {
        return actionNames.keySet();
    }

    public static Set getActionNames() {
        return actionTypes.keySet();
    }

    public static Class getActionType(String name) {
        return (Class) actionTypes.get(name);
    }

    public static String getActionName(Class type) {
        return (String) actionNames.get(type);
    }

    public static boolean hasActionName(String name) {
        return actionTypes.containsKey(name);
    }

    static final Log log = LogFactory.getLog(ActionTypes.class);
    static Map<String, Class<? extends Action>> actionTypes = initialiseActionTypes();
    static Map actionNames = NodeTypes.createInverseMapping(actionTypes);

    static Map<String, Class<? extends Action>> initialiseActionTypes() {
        Map<String, Class<? extends Action>> types = Maps.newHashMap();
        // TODO move to spring
        types.put("action", Action.class);
        types.put("create-timer", CreateTimerAction.class);
        types.put("cancel-timer", CancelTimerAction.class);
        return types;
    }
}

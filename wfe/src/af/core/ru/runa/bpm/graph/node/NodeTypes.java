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
package ru.runa.bpm.graph.node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.bpm.graph.def.Node;

import com.google.common.collect.Maps;

public class NodeTypes {

    public static Set getNodeTypes() {
        return nodeTypes.keySet();
    }

    public static Set getNodeNames() {
        return nodeNames.keySet();
    }

    public static Class getNodeType(String name) {
        return nodeTypes.get(name);
    }

    public static String getNodeName(Class type) {
        return (String) nodeNames.get(type);
    }

    private static final Log log = LogFactory.getLog(NodeTypes.class);

    static Map<String, Class<? extends Node>> nodeTypes = initialiseNodeTypes();
    static Map nodeNames = createInverseMapping(nodeTypes);

    static Map<String, Class<? extends Node>> initialiseNodeTypes() {
        Map<String, Class<? extends Node>> types = Maps.newHashMap();
        // TODO move to Spring
        types.put("start-state", StartState.class);
        types.put("end-state", EndState.class);
        types.put("node", Node.class);
        types.put("state", State.class);
        types.put("task-node", TaskNode.class);
        types.put("fork", Fork.class);
        types.put("join", Join.class);
        types.put("decision", Decision.class);
        types.put("process-state", ProcessState.class);
        types.put("multiinstance-state", MultiInstanceState.class);
        // types.put("super-state", SuperState.class);
        types.put("send-message", SendMessage.class);
        types.put("receive-message", ReceiveMessage.class);
        return types;
    }

    public static Map createInverseMapping(Map map) {
        Map names = new HashMap();
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            names.put(entry.getValue(), entry.getKey());
        }
        return names;
    }
}

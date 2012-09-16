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

import org.dom4j.Element;

import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;

public class StartState extends InteractionNode {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.StartState;
    }

    private static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_NODE_LEAVE, Event.EVENTTYPE_AFTER_SIGNAL };

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element startStateElement, JpdlXmlReader jpdlReader) {
        // if the start-state has a task specified,
        Element startTaskElement = startStateElement.element("task");
        if (startTaskElement != null) {
            // delegate the parsing of the start-state task to the jpdlReader
            jpdlReader.readStartStateTask(processDefinition, startTaskElement, this);
        }
    }

    @Override
    public void execute(ExecutionContext executionContext) {
    }

    @Override
    public Transition addArrivingTransition(Transition t) {
        throw new UnsupportedOperationException("illegal operation : its not possible to add a transition that is arriving in a start state");
    }

}

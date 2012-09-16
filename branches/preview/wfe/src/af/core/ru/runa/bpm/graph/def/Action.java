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

import org.dom4j.Element;

import ru.runa.ConfigurationException;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.instantiation.Delegation;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.jpdl.xml.Parsable;
import ru.runa.wf.ProcessDefinitionXMLFormatException;

public class Action implements ActionHandler, Parsable {
    private static final long serialVersionUID = 1L;
    private String name;
    private boolean propagationAllowed = true;
    private Delegation delegation;
    private Event event;

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element actionElement, JpdlXmlReader jpdlReader) {
        if (actionElement.attribute("class") != null) {
            delegation = new Delegation();
            delegation.read(processDefinition, actionElement, jpdlReader);
        } else {
            throw new ProcessDefinitionXMLFormatException("action does not have class attribute " + actionElement.asXML());
        }

        String acceptPropagatedEvents = actionElement.attributeValue("accept-propagated-events");
        if ("false".equalsIgnoreCase(acceptPropagatedEvents)) {
            propagationAllowed = false;
        }
    }

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        ActionHandler actionHandler = (ActionHandler) delegation.getInstance();
        actionHandler.execute(executionContext);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPropagationAllowed() {
        return propagationAllowed;
    }

    public String getName() {
        return name;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Delegation getDelegation() {
        return delegation;
    }

    public void setDelegation(Delegation instantiatableDelegate) {
        this.delegation = instantiatableDelegate;
    }

}

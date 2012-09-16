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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.instantiation.Delegation;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.wf.ProcessDefinitionXMLFormatException;

/**
 * decision node.
 */
public class Decision extends Node {
    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(Decision.class);

    private Delegation decisionDelegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.Decision;
    }

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element decisionElement, JpdlXmlReader jpdlReader) {
        Element decisionHandlerElement = decisionElement.element("handler");
        if (decisionHandlerElement != null) {
            decisionDelegation = new Delegation();
            decisionDelegation.read(processDefinition, decisionHandlerElement, jpdlReader);
        } else {
            throw new ProcessDefinitionXMLFormatException("No handler in decision found: " + getName());
        }
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            DecisionHandler decisionHandler = (DecisionHandler) decisionDelegation.getInstance();
            if (decisionHandler == null) {
                decisionHandler = (DecisionHandler) decisionDelegation.getInstance();
            }
            String transitionName = decisionHandler.decide(executionContext);
            Transition transition = getLeavingTransition(transitionName);
            if (transition == null) {
                throw new InternalApplicationException("decision '" + name + "' selected non existing transition '" + transitionName + "'");
            }
            log.debug("decision " + name + " is taking '" + transition + "'");
            executionContext.leaveNode(transition);
        } catch (Exception exception) {
            raiseException(exception, executionContext);
        }
    }

}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.node.StartState;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.commons.format.FormatCommons;
import ru.runa.commons.format.WebFormat;
import ru.runa.wf.ProcessDefinitionFileDoesNotExistException;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.form.VariableDefinition;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ExecutableProcessDefinition extends GraphElement implements NodeCollection, Serializable {
    private static final long serialVersionUID = 1L;
    private final ArchievedProcessDefinition dBImpl;
    private Map<String, byte[]> processFiles = Maps.newHashMap();
    private StartState startState;
    private final List<Node> nodes = Lists.newArrayList();
    private final Map<String, Swimlane> swimlanes = Maps.newHashMap();
    private final Map<String, Interaction> interactions = Maps.newHashMap();
    private final Map<String, VariableDefinition> variables = Maps.newHashMap();

    private static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_PROCESS_START, Event.EVENTTYPE_PROCESS_END,
            Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE, Event.EVENTTYPE_TASK_CREATE, Event.EVENTTYPE_TASK_ASSIGN,
            Event.EVENTTYPE_TASK_START, Event.EVENTTYPE_TASK_END, Event.EVENTTYPE_TRANSITION, Event.EVENTTYPE_BEFORE_SIGNAL,
            Event.EVENTTYPE_AFTER_SIGNAL, Event.EVENTTYPE_SUPERSTATE_ENTER, Event.EVENTTYPE_SUPERSTATE_LEAVE, Event.EVENTTYPE_SUBPROCESS_CREATED,
            Event.EVENTTYPE_SUBPROCESS_END, Event.EVENTTYPE_TIMER };

    public ExecutableProcessDefinition(ArchievedProcessDefinition processDefinitionDBImpl) {
        this.dBImpl = processDefinitionDBImpl;
        this.processDefinition = this;
    }

    public Long getId() {
        return dBImpl.getId();
    }

    @Override
    public String getName() {
        return dBImpl.getName();
    }

    @Override
    public void setName(String name) {
        dBImpl.setName(name);
    }

    @Override
    public String getDescription() {
        return dBImpl.getDescription();
    }

    @Override
    public void setDescription(String description) {
        dBImpl.setDescription(description);
    }

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    public ArchievedProcessDefinition getDBImpl() {
        return dBImpl;
    }

    public ProcessInstance createProcessInstance(Map<String, Object> variables) {
        return new ProcessInstance(this, variables);
    }

    /**
     * add a file to this definition.
     */
    public void addFile(String name, byte[] bytes) {
        processFiles.put(name, bytes);
    }

    public void addInteraction(String name, Interaction interaction) {
        interactions.put(name, interaction);
    }

    public void addVariable(String name, VariableDefinition variableDefinition) {
        variables.put(name, variableDefinition);
    }

    public VariableDefinition getVariable(String name) {
        return variables.get(name);
    }

    public boolean isVariablePublic(String name) {
        VariableDefinition variableDefinition = getVariable(name);
        if (variableDefinition == null) {
            return false;
        }
        return variableDefinition.isPublic();
    }

    public Map<String, VariableDefinition> getVariables() {
        return variables;
    }

    public Interaction getInteraction(String stateName) {
        return interactions.get(stateName);
    }

    public Interaction getInteractionNotNull(String stateName) {
        Interaction interaction = getInteraction(stateName);
        // TODO Preconditions.checkNotNull(interaction,
        // "No interaction found for state " + stateName);
        if (interaction == null) {
            interaction = new Interaction(null, stateName, null, null, false, null);
        }
        return interaction;
    }

    public byte[] getFileBytes(String fileName) {
        Preconditions.checkNotNull(fileName, "fileName");
        return processFiles.get(fileName);
    }

    public byte[] getFileBytesNotNull(String fileName) {
        byte[] bytes = getFileBytes(fileName);
        if (bytes == null) {
            throw new ProcessDefinitionFileDoesNotExistException(fileName);
        }
        return bytes;
    }

    public Map<String, Object> getDefaultVariableValues() {
        Map<String, Object> result = new HashMap<String, Object>();
        for (VariableDefinition variableDefinition : variables.values()) {
            if (variableDefinition.getDefaultValue() != null) {
                try {
                    WebFormat webFormat = FormatCommons.create(variableDefinition.getFormat());
                    Object value = webFormat.parse(new String[] { variableDefinition.getDefaultValue() });
                    result.put(variableDefinition.getName(), value);
                } catch (Exception e) {
                    // log.warn(
                    // "Unable to assign default value '" +
                    // variableDefinition.getDefaultValue() + "' of type " +
                    // variableDefinition.getFormat()
                    // TODO + " to " + variableDefinition.getName(), e);
                }
            }
        }
        return result;
    }

    @Transient
    public StartState getStartState() {
        return startState;
    }

    @Transient
    public StartState getStartStateNotNull() {
        Preconditions.checkNotNull(startState, "startState");
        return startState;
    }

    public void setStartState(StartState startState) {
        this.startState = startState;
    }

    @Transient
    @Override
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public Node addNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("can't add a null node to a processdefinition");
        }
        nodes.add(node);
        node.processDefinition = this;

        if (this.startState == null && node instanceof StartState) {
            this.startState = (StartState) node;
        }
        return node;
    }

    @Override
    public Node findNode(String hierarchicalName) {
        // return findNode(this, hierarchicalName);
        for (Node node : nodes) {
            if (hierarchicalName.equals(node.getName())) {
                return node;
            }
        }
        return null;
    }

    @Transient
    @Override
    public GraphElement getParent() {
        return null;
    }

    public void addSwimlane(Swimlane swimlane) {
        swimlanes.put(swimlane.getName(), swimlane);
    }

    @Transient
    public Map<String, Swimlane> getSwimlanes() {
        return swimlanes;
    }

    public Swimlane getSwimlane(String swimlaneName) {
        return swimlanes.get(swimlaneName);
    }

    public Swimlane getSwimlaneNotNull(String swimlaneName) {
        Swimlane swimlane = getSwimlane(swimlaneName);
        if (swimlane == null) {
            throw new InternalApplicationException("non-existing swimlane " + swimlaneName);
        }
        return swimlane;
    }

}

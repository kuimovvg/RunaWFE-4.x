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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ProcessDefinition extends GraphElement implements IFileDataProvider {
    private static final long serialVersionUID = 1L;

    protected Deployment deployment;
    protected Map<String, byte[]> processFiles = Maps.newHashMap();
    protected StartState startState;
    protected final List<Node> nodes = Lists.newArrayList();
    protected final Map<String, SwimlaneDefinition> swimlaneDefinitions = Maps.newHashMap();
    protected final Map<String, Interaction> interactions = Maps.newHashMap();
    protected final List<VariableDefinition> variables = Lists.newArrayList();
    protected final Map<String, VariableDefinition> variablesMap = Maps.newHashMap();
    /**
     * @deprecated remove in 4.2.0
     */
    protected final Set<String> taskNamesToignoreSubstitutionRules = Sets.newHashSet();
    protected ProcessDefinitionAccessType accessType = ProcessDefinitionAccessType.Process;
    protected Map<String, SubprocessDefinition> embeddedSubprocesses = Maps.newHashMap();

    private static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_PROCESS_START, Event.EVENTTYPE_PROCESS_END,
            Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE, Event.EVENTTYPE_TASK_CREATE, Event.EVENTTYPE_TASK_ASSIGN,
            Event.EVENTTYPE_TASK_START, Event.EVENTTYPE_TASK_END, Event.EVENTTYPE_TRANSITION, Event.EVENTTYPE_BEFORE_SIGNAL,
            Event.EVENTTYPE_AFTER_SIGNAL, Event.EVENTTYPE_SUPERSTATE_ENTER, Event.EVENTTYPE_SUPERSTATE_LEAVE, Event.EVENTTYPE_SUBPROCESS_START,
            Event.EVENTTYPE_SUBPROCESS_END, Event.EVENTTYPE_TIMER };

    protected ProcessDefinition() {
    }

    public ProcessDefinition(Deployment deployment) {
        this.deployment = deployment;
        processDefinition = this;
    }

    public Long getId() {
        return deployment.getId();
    }

    @Override
    public String getName() {
        return deployment.getName();
    }

    @Override
    public void setName(String name) {
        deployment.setName(name);
    }

    @Override
    public String getDescription() {
        return deployment.getDescription();
    }

    @Override
    public void setDescription(String description) {
        deployment.setDescription(description);
    }

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public ProcessDefinitionAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(ProcessDefinitionAccessType accessType) {
        this.accessType = accessType;
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
        variablesMap.put(name, variableDefinition);
        variables.add(variableDefinition);
    }

    public VariableDefinition getVariable(String name, boolean searchInSwimlanes) {
        VariableDefinition variableDefinition = variablesMap.get(name);
        if (variableDefinition == null) {
            SwimlaneDefinition swimlaneDefinition = getSwimlane(name);
            if (swimlaneDefinition != null) {
                variableDefinition = swimlaneDefinition.toVariableDefinition();
            }
        }
        return variableDefinition;
    }

    public VariableDefinition getVariableNotNull(String name, boolean searchInSwimlanes) {
        VariableDefinition variableDefinition = getVariable(name, searchInSwimlanes);
        if (variableDefinition == null) {
            throw new InternalApplicationException("variable '" + name + "' not found in " + this);
        }
        return variableDefinition;
    }

    public List<VariableDefinition> getVariables() {
        return variables;
    }

    public Interaction getInteractionNotNull(String nodeId) {
        Interaction interaction = interactions.get(nodeId);
        if (interaction == null) {
            InteractionNode node = (InteractionNode) getNodeNotNull(nodeId);
            interaction = new Interaction(node.getName(), node.getDescription(), null, null, null, false, null, null);
        }
        return interaction;
    }

    @Override
    public byte[] getFileData(String fileName) {
        Preconditions.checkNotNull(fileName, "fileName");
        return processFiles.get(fileName);
    }

    @Override
    public byte[] getFileDataNotNull(String fileName) {
        byte[] bytes = getFileData(fileName);
        if (bytes == null) {
            throw new DefinitionFileDoesNotExistException(fileName);
        }
        return bytes;
    }

    public byte[] getGraphImageBytes() {
        byte[] graphBytes = processDefinition.getFileData(IFileDataProvider.GRAPH_IMAGE_NEW_FILE_NAME);
        if (graphBytes == null) {
            graphBytes = processDefinition.getFileData(IFileDataProvider.GRAPH_IMAGE_OLD_FILE_NAME);
        }
        if (graphBytes == null) {
            throw new InternalApplicationException("Neither " + IFileDataProvider.GRAPH_IMAGE_NEW_FILE_NAME + " and "
                    + IFileDataProvider.GRAPH_IMAGE_OLD_FILE_NAME + " not found in process");
        }
        return graphBytes;
    }

    public Map<String, Object> getDefaultVariableValues() {
        Map<String, Object> result = new HashMap<String, Object>();
        for (VariableDefinition variableDefinition : variables) {
            if (variableDefinition.getDefaultValue() != null) {
                try {
                    VariableFormat variableFormat = FormatCommons.create(variableDefinition);
                    Object value = variableFormat.parse(variableDefinition.getDefaultValue());
                    result.put(variableDefinition.getName(), value);
                } catch (Exception e) {
                    log.warn("Unable to get default value '" + variableDefinition.getDefaultValue() + "' in " + variableDefinition, e);
                }
            }
        }
        return result;
    }

    public StartState getStartStateNotNull() {
        Preconditions.checkNotNull(startState, "startState");
        return startState;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node addNode(Node node) {
        Preconditions.checkArgument(node != null, "can't add a null node to a processdefinition");
        nodes.add(node);
        node.processDefinition = this;
        if (node instanceof StartState) {
            if (startState != null) {
                throw new InvalidDefinitionException(getName(), "only one start-state allowed in a process");
            }
            startState = (StartState) node;
        }
        return node;
    }

    public Node getNode(String id) {
        Preconditions.checkNotNull(id);
        for (Node node : nodes) {
            if (id.equals(node.getNodeId())) {
                return node;
            }
        }
        return null;
    }

    public Node getNodeNotNull(String id) {
        Preconditions.checkNotNull(id);
        Node node = getNode(id);
        if (node != null) {
            return node;
        }
        throw new InternalApplicationException("node '" + id + "' not found");
    }

    public GraphElement getGraphElement(String id) {
        for (Node node : nodes) {
            if (id.equals(node.getNodeId())) {
                return node;
            }
            Action action = node.getAction(id);
            if (action != null) {
                return action;
            }
        }
        return null;
    }

    public GraphElement getGraphElementNotNull(String id) {
        GraphElement graphElement = getGraphElement(id);
        if (graphElement == null) {
            throw new InternalApplicationException("element '" + id + "' not found");
        }
        return graphElement;
    }

    @Override
    public GraphElement getParent() {
        return null;
    }

    public void addSwimlane(SwimlaneDefinition swimlaneDefinition) {
        swimlaneDefinitions.put(swimlaneDefinition.getName(), swimlaneDefinition);
    }

    public Map<String, SwimlaneDefinition> getSwimlanes() {
        return swimlaneDefinitions;
    }

    public SwimlaneDefinition getSwimlane(String swimlaneName) {
        return swimlaneDefinitions.get(swimlaneName);
    }

    public SwimlaneDefinition getSwimlaneNotNull(String swimlaneName) {
        SwimlaneDefinition swimlaneDefinition = getSwimlane(swimlaneName);
        if (swimlaneDefinition == null) {
            throw new InternalApplicationException("swimlane '" + swimlaneName + "' not found in " + this);
        }
        return swimlaneDefinition;
    }

    public TaskDefinition getTaskNotNull(String nodeId) {
        for (Node node : getNodes()) {
            if (node instanceof InteractionNode) {
                for (TaskDefinition taskDefinition : ((InteractionNode) node).getTasks()) {
                    if (Objects.equal(nodeId, taskDefinition.getNodeId())) {
                        return taskDefinition;
                    }
                }
            }
        }
        throw new InternalApplicationException("task '" + nodeId + "' not found in " + this);
    }

    public boolean ignoreSubsitutionRulesForTask(Task task) {
        if (taskNamesToignoreSubstitutionRules.contains(task.getNodeId())) {
            return true;
        }
        InteractionNode interactionNode = (InteractionNode) getNodeNotNull(task.getNodeId());
        return interactionNode.getFirstTaskNotNull().isIgnoreSubsitutionRules();
    }

    public void addTaskNameToignoreSubstitutionRules(String nodeId) {
        taskNamesToignoreSubstitutionRules.add(nodeId);
    }

    @Override
    public String toString() {
        if (deployment != null) {
            return deployment.toString();
        }
        return name;
    }

    public void addEmbeddedSubprocess(SubprocessDefinition subprocessDefinition) {
        embeddedSubprocesses.put(subprocessDefinition.getNodeId(), subprocessDefinition);
    }

    public SubprocessDefinition getEmbeddedSubprocessesById(String id) {
        return embeddedSubprocesses.get(id);
    }

    public SubprocessDefinition getEmbeddedSubprocessesByName(String name) {
        for (SubprocessDefinition subprocessDefinition : embeddedSubprocesses.values()) {
            if (Objects.equal(name, subprocessDefinition.getName())) {
                return subprocessDefinition;
            }
        }
        return null;
    }

    public void mergeWithEmbeddedSubprocesses() {
        for (Node node : Lists.newArrayList(nodes)) {
            if (node instanceof SubProcessState) {
                SubProcessState subProcessState = (SubProcessState) node;
                if (subProcessState.isEmbedded()) {
                    SubprocessDefinition subprocessDefinition = getEmbeddedSubprocessesByName(subProcessState.getSubProcessName());
                    Preconditions.checkNotNull(subprocessDefinition, "subprocessDefinition");
                    subprocessDefinition.setSubProcessState(subProcessState); // TODO
                    Transition leavingTransition = subProcessState.getLeavingTransitions().get(0);
                    subProcessState.getLeavingTransitions().clear();
                    subProcessState.addLeavingTransition(subprocessDefinition.getStartStateNotNull().getLeavingTransitions().get(0));
                    List<EmbeddedSubprocessEndNode> endNodes = subprocessDefinition.getEndNodes();
                    // FIXME duplicate leaving transitions for multiple end
                    // nodes?
                    for (EmbeddedSubprocessEndNode endNode : endNodes) {
                        endNode.addLeavingTransition(leavingTransition);
                    }
                    subprocessDefinition.setArrivingNode(leavingTransition.getTo()); // TODO
                    processDefinition.getNodes().addAll(subprocessDefinition.getNodes());
                }
            }
        }
    }
}

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
package ru.runa.bpm.jpdl.xml;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ru.runa.bpm.context.def.VariableMapping;
import ru.runa.bpm.graph.action.ActionTypes;
import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.GraphElement;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.NodeCollection;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.node.InteractionNode;
import ru.runa.bpm.graph.node.NodeTypes;
import ru.runa.bpm.graph.node.StartState;
import ru.runa.bpm.graph.node.TaskNode;
import ru.runa.bpm.par.FileDataProvider;
import ru.runa.bpm.par.InvalidProcessDefinition;
import ru.runa.bpm.scheduler.def.CancelTimerAction;
import ru.runa.bpm.scheduler.def.CreateTimerAction;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.wf.ProcessDefinitionXMLFormatException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

@SuppressWarnings("rawtypes")
public class JpdlXmlReader {
    private final byte[] definitionXml;
    private String defaultDueDate;
    private final List<Object[]> unresolvedTransitionDestinations = Lists.newArrayList();

    /**
     * the parsed process definition as DOM tree (available after
     * readProcessDefinition)
     */
    protected Document document;

    /** for autonumbering anonymous timers. */
    private int timerNumber;

    public JpdlXmlReader(byte[] definitionXml) {
        this.definitionXml = definitionXml;
    }

    public ExecutableProcessDefinition readProcessDefinition(ExecutableProcessDefinition processDefinition) {
        try {
            // TODO validation
            // XMLHelper.getDocument(new ByteArrayInputStream(definitionXml));
            // parse the document into a dom tree
            document = DocumentHelper.parseText(new String(definitionXml, Charsets.UTF_8));
            Element root = document.getRootElement();

            // read the process name
            processDefinition.setName(root.attributeValue("name"));
            defaultDueDate = root.attributeValue("default-task-duedate");
            if ("true".equals(root.attributeValue("invalid"))) {
                throw new InvalidProcessDefinition("invalid process definition");
            }

            // get the process description
            String description = root.elementTextTrim("description");
            if (description != null) {
                processDefinition.setDescription(description);
            }

            // first pass: read most content
            readSwimlanes(processDefinition, root);
            readActions(processDefinition, root, null, null);
            readNodes(processDefinition, root, processDefinition);
            readEvents(processDefinition, root, processDefinition);
            readExceptionHandlers(root, processDefinition);
            readTasks(processDefinition, root, null);

            // second pass processing
            resolveTransitionDestinations(processDefinition);
            verifySwimlaneAssignments(processDefinition);
        } catch (Exception e) {
            throw new ProcessDefinitionXMLFormatException(FileDataProvider.PROCESSDEFINITION_XML_FILE_NAME, e);
        }
        return processDefinition;
    }

    private void readSwimlanes(ExecutableProcessDefinition processDefinition, Element processDefinitionElement) {
        Iterator iter = processDefinitionElement.elementIterator("swimlane");
        while (iter.hasNext()) {
            Element swimlaneElement = (Element) iter.next();
            String swimlaneName = swimlaneElement.attributeValue("name");
            if (swimlaneName == null) {
                throw new InvalidProcessDefinition("there's a swimlane without a name");
            } else {
                Swimlane swimlane = new Swimlane(swimlaneName);
                Element assignmentElement = swimlaneElement.element("assignment");
                if (assignmentElement != null) {
                    swimlane.read(processDefinition, assignmentElement, this);
                } else {
                    Task startTask = processDefinition.getStartStateNotNull().getFirstTaskNotNull();
                    if (startTask == null || startTask.getSwimlane() != swimlane) {
                        throw new InvalidProcessDefinition("swimlane '" + swimlaneName + "' does not have an assignment");
                    }
                }
                processDefinition.addSwimlane(swimlane);
            }
        }
    }

    private void readNodes(ExecutableProcessDefinition processDefinition, Element element, NodeCollection nodeCollection) {
        Iterator nodeElementIter = element.elementIterator();
        while (nodeElementIter.hasNext()) {
            Element nodeElement = (Element) nodeElementIter.next();
            String nodeName = nodeElement.getName();
            // get the node type
            Class<? extends Node> nodeType = NodeTypes.getNodeType(nodeName);
            if (nodeType != null) {
                Node node = null;
                try {
                    // create a new instance
                    node = nodeType.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("couldn't instantiate node '" + nodeName + "', of type '" + nodeType.getName() + "'", e);
                }

                node.setProcessDefinition(processDefinition);

                // check for duplicate start-states
                if (node instanceof StartState && processDefinition.getStartState() != null) {
                    throw new InvalidProcessDefinition("max one start-state allowed in a process");
                } else {
                    // read the common node parts of the element
                    readNode(processDefinition, nodeElement, node, nodeCollection);

                    // if the node is parsable
                    // (meaning: if the node has special configuration to parse,
                    // other then the
                    // common node data)
                    node.read(processDefinition, nodeElement, this);

                    readNodeTimers(processDefinition, nodeElement, node);
                }
            }
        }
    }

    public void readTasks(ExecutableProcessDefinition processDefinition, Element element, TaskNode taskNode) {
        List elements = element.elements("task");
        if (elements.size() > 0) {
            Iterator iter = elements.iterator();
            while (iter.hasNext()) {
                Element taskElement = (Element) iter.next();
                readTask(processDefinition, taskElement, taskNode);
            }
        }
    }

    private Task readTask(ExecutableProcessDefinition processDefinition, Element taskElement, InteractionNode node) {
        Task task = new Task();
        task.setProcessDefinition(processDefinition);

        // get the task name
        String name = taskElement.attributeValue("name");
        if (name != null) {
            task.setName(name);
        } else {
            task.setName(node.getName());
        }

        // get the task description
        String description = taskElement.elementTextTrim("description");
        if (description != null) {
            task.setDescription(description);
        } else {
            task.setDescription(taskElement.attributeValue("description"));
        }

        // parse common subelements
        readNodeTimers(processDefinition, taskElement, task);
        readEvents(processDefinition, taskElement, task);
        readExceptionHandlers(taskElement, task);

        // duedate and priority
        String duedateText = taskElement.attributeValue("duedate");
        if (duedateText == null) {
            duedateText = taskElement.attributeValue("dueDate");
        }
        if (duedateText == null) {
            duedateText = defaultDueDate;
        }
        task.setDueDate(duedateText);
        String priorityText = taskElement.attributeValue("priority");
        if (priorityText != null) {
            task.setPriority(Task.parsePriority(priorityText));
        }

        node.addTask(task);

        // assignment
        String swimlaneName = taskElement.attributeValue("swimlane");

        // if there is a swimlane attribute specified
        if (swimlaneName != null) {
            Swimlane swimlane = processDefinition.getSwimlaneNotNull(swimlaneName);
            task.setSwimlane(swimlane);

            String reassignAttr = taskElement.attributeValue("reassign");
            // if there is a reassign attribute specified
            if (reassignAttr != null) {
                task.setReassignSwimlane(Boolean.valueOf(reassignAttr));
            }

            // if no assignment or swimlane is specified
        } else {
            // the user has to manage assignment manually, so we better inform
            // him/her.
            throw new InvalidProcessDefinition("process xml information: no swimlane or assignment specified for task '" + taskElement.asXML() + "'");
        }
        return task;
    }

    public List<VariableMapping> readVariableMappings(Element element) {
        List<VariableMapping> variableAccesses = Lists.newArrayList();
        Iterator iter = element.elementIterator("variable");
        while (iter.hasNext()) {
            Element variableElement = (Element) iter.next();

            String variableName = variableElement.attributeValue("name");
            if (variableName == null) {
                throw new InvalidProcessDefinition("the name attribute of a variable element is required: " + variableElement.asXML());
            }
            String mappedName = variableElement.attributeValue("mapped-name");
            if (mappedName == null) {
                throw new InvalidProcessDefinition("the mapped-name attribute of a variable element is required: " + variableElement.asXML());
            }
            String access = variableElement.attributeValue("access", "read,write");
            variableAccesses.add(new VariableMapping(variableName, mappedName, access));
        }
        return variableAccesses;
    }

    public void readStartStateTask(ExecutableProcessDefinition processDefinition, Element startTaskElement, StartState startState) {
        readTask(processDefinition, startTaskElement, startState);
        processDefinition.setStartState(startState);
    }

    private void readNode(ExecutableProcessDefinition processDefinition, Element nodeElement, Node node, NodeCollection nodeCollection) {
        // first put the node in its collection. this is done so that the
        // setName later on will be able to differentiate between nodes
        // contained in
        // processDefinitions and nodes contained in superstates
        nodeCollection.addNode(node);

        // get the node name
        String name = nodeElement.attributeValue("name");
        if (name != null) {
            node.setName(name);
        }

        // get the node description
        String description = nodeElement.elementTextTrim("description");
        if (description != null) {
            node.setDescription(description);
        }

        // parse common subelements
        readEvents(processDefinition, nodeElement, node);
        readExceptionHandlers(nodeElement, node);

        // save the transitions and parse them at the end
        addUnresolvedTransitionDestination(nodeElement, node);
    }

    private void readNodeTimers(ExecutableProcessDefinition processDefinition, Element nodeElement, GraphElement node) {
        Iterator iter = nodeElement.elementIterator("timer");
        while (iter.hasNext()) {
            Element element = (Element) iter.next();
            readNodeTimer(processDefinition, element, node);
        }
    }

    private void readNodeTimer(ExecutableProcessDefinition processDefinition, Element timerElement, GraphElement node) {
        String name = timerElement.attributeValue("name", node.getName());
        if (name == null) {
            name = generateTimerName();
        }

        CreateTimerAction createTimerAction = new CreateTimerAction();

        if (TaskNode.class.isInstance(node)) {
            String dueDate = ((TaskNode) node).getFirstTaskNotNull().getDueDate();
            createTimerAction.setDefaultDueDate(dueDate);
        }

        createTimerAction.read(processDefinition, timerElement, this);
        createTimerAction.setTimerName(name);
        createTimerAction.setTimerAction(readSingleAction(processDefinition, timerElement));
        String createEventType = node instanceof Task ? Event.EVENTTYPE_TASK_CREATE : Event.EVENTTYPE_NODE_ENTER;
        addAction(node, createEventType, createTimerAction);

        CancelTimerAction cancelTimerAction = new CancelTimerAction();
        cancelTimerAction.setTimerName(name);
        String cancelEventType = node instanceof Task ? Event.EVENTTYPE_TASK_END : Event.EVENTTYPE_NODE_LEAVE;
        addAction(node, cancelEventType, cancelTimerAction);
    }

    private String generateTimerName() {
        return "timer-" + (timerNumber++);
    }

    private void readEvents(ExecutableProcessDefinition processDefinition, Element parentElement, GraphElement graphElement) {
        Iterator iter = parentElement.elementIterator("event");
        while (iter.hasNext()) {
            Element eventElement = (Element) iter.next();
            String eventType = eventElement.attributeValue("type");
            if (!graphElement.hasEvent(eventType)) {
                graphElement.addEvent(new Event(eventType));
            }
            readActions(processDefinition, eventElement, graphElement, eventType);
        }
    }

    private void readActions(ExecutableProcessDefinition processDefinition, Element eventElement, GraphElement graphElement, String eventType) {
        // for all the elements in the event element
        Iterator nodeElementIter = eventElement.elementIterator();
        while (nodeElementIter.hasNext()) {
            Element actionElement = (Element) nodeElementIter.next();
            String actionName = actionElement.getName();
            if (ActionTypes.hasActionName(actionName)) {
                Action action = createAction(processDefinition, actionElement);
                if ((graphElement != null) && (eventType != null)) {
                    // add the action to the event
                    addAction(graphElement, eventType, action);
                }
            }
        }
    }

    private void addAction(GraphElement graphElement, String eventType, Action action) {
        Event event = graphElement.getEvent(eventType);
        if (event == null) {
            event = new Event(eventType);
            graphElement.addEvent(event);
        }
        event.addAction(action);
    }

    public Action readSingleAction(ExecutableProcessDefinition processDefinition, Element nodeElement) {
        Action action = null;
        // search for the first action element in the node
        Iterator iter = nodeElement.elementIterator();
        while (iter.hasNext() && (action == null)) {
            Element candidate = (Element) iter.next();
            if (ActionTypes.hasActionName(candidate.getName())) {
                // parse the action and assign it to this node
                action = createAction(processDefinition, candidate);
            }
        }
        return action;
    }

    private Action createAction(ExecutableProcessDefinition processDefinition, Element actionElement) {
        // create a new instance of the action
        Action action = null;
        String actionName = actionElement.getName();
        Class actionType = ActionTypes.getActionType(actionName);
        try {
            action = (Action) actionType.newInstance();
        } catch (Exception e) {
            throw new InvalidProcessDefinition("couldn't instantiate action '" + actionName + "', of type '" + actionType.getName() + "'", e);
        }

        // read the common node parts of the action
        readAction(processDefinition, actionElement, action);

        return action;
    }

    private void readAction(ExecutableProcessDefinition processDefinition, Element element, Action action) {
        action.setName(element.attributeValue("name"));
        action.read(processDefinition, element, this);
    }

    private void readExceptionHandlers(Element graphElementElement, GraphElement graphElement) {
    }

    // transition destinations are parsed in a second pass
    // //////////////////////

    private void addUnresolvedTransitionDestination(Element nodeElement, Node node) {
        unresolvedTransitionDestinations.add(new Object[] { nodeElement, node });
    }

    private void resolveTransitionDestinations(ExecutableProcessDefinition processDefinition) {
        Iterator iter = unresolvedTransitionDestinations.iterator();
        while (iter.hasNext()) {
            Object[] unresolvedTransition = (Object[]) iter.next();
            Element nodeElement = (Element) unresolvedTransition[0];
            Node node = (Node) unresolvedTransition[1];
            resolveTransitionDestinations(processDefinition, nodeElement.elements("transition"), node);
        }
    }

    private void resolveTransitionDestinations(ExecutableProcessDefinition processDefinition, List transitionElements, Node node) {
        Iterator iter = transitionElements.iterator();
        while (iter.hasNext()) {
            Element transitionElement = (Element) iter.next();
            resolveTransitionDestination(processDefinition, transitionElement, node);
        }
    }

    /**
     * creates the transition object and configures it by the read attributes
     * 
     * @return the created <code>ru.runa.bpm.graph.def.Transition</code> object
     *         (useful, if you want to override this method to read additional
     *         configuration properties)
     */
    private Transition resolveTransitionDestination(ExecutableProcessDefinition processDefinition, Element transitionElement, Node node) {
        Transition transition = new Transition();
        transition.setProcessDefinition(processDefinition);

        transition.setName(transitionElement.attributeValue("name"));
        transition.setDescription(transitionElement.elementTextTrim("description"));

        // add the transition to the node
        node.addLeavingTransition(transition);

        // set destinationNode of the transition
        String toName = transitionElement.attributeValue("to");
        if (toName == null) {
            throw new InvalidProcessDefinition("node '" + node.getFullyQualifiedName()
                    + "' has a transition without a 'to'-attribute to specify its destinationNode");
        } else {
            Node to = ((NodeCollection) node.getParent()).findNode(toName);
            if (to == null) {
                throw new InvalidProcessDefinition("transition to='" + toName + "' on node '" + node.getFullyQualifiedName() + "' cannot be resolved");
            } else {
                to.addArrivingTransition(transition);
            }
        }

        // read the actions
        readActions(processDefinition, transitionElement, transition, Event.EVENTTYPE_TRANSITION);

        readExceptionHandlers(transitionElement, transition);

        return transition;
    }

    // verify swimlane assignments in second pass
    // ///////////////////////////////
    private void verifySwimlaneAssignments(ExecutableProcessDefinition processDefinition) {
        Task startTask = processDefinition.getStartStateNotNull().getFirstTaskNotNull();
        for (Swimlane swimlane : processDefinition.getSwimlanes().values()) {
            Swimlane startTaskSwimlane = (startTask != null ? startTask.getSwimlane() : null);
            if (swimlane.getDelegation() == null && swimlane != startTaskSwimlane) {
                throw new InvalidProcessDefinition("swimlane '" + swimlane.getName() + "' does not have an assignment");
            }
        }
    }

}

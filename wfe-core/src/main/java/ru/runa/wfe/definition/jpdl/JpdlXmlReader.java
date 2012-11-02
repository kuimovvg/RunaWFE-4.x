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
package ru.runa.wfe.definition.jpdl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.job.CancelTimerAction;
import ru.runa.wfe.job.CreateTimerAction;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.ActionNode;
import ru.runa.wfe.lang.Decision;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.EndNode;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.Fork;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.MultiProcessState;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.ReceiveMessage;
import ru.runa.wfe.lang.SendMessage;
import ru.runa.wfe.lang.StartState;
import ru.runa.wfe.lang.SubProcessState;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.VariableContainerNode;
import ru.runa.wfe.lang.WaitState;
import ru.runa.wfe.lang.jpdl.Join;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings({"rawtypes","unchecked"})
public class JpdlXmlReader {
    private final byte[] definitionXml;
    private String defaultDueDate;
    private final List<Object[]> unresolvedTransitionDestinations = Lists.newArrayList();

    private Document document;
    // TODO move to Spring (or GPD process setting)
    private boolean waitStateCompatibility = true;

    private static Map<String, Class<? extends Node>> nodeTypes = Maps.newHashMap();
    static {
        nodeTypes.put("start-state", StartState.class);
        nodeTypes.put("end-state", EndNode.class);
        nodeTypes.put("node", ActionNode.class);
        nodeTypes.put("wait-state", WaitState.class);
        nodeTypes.put("task-node", TaskNode.class);
        nodeTypes.put("fork", Fork.class);
        nodeTypes.put("join", Join.class);
        nodeTypes.put("decision", Decision.class);
        nodeTypes.put("process-state", SubProcessState.class);
        nodeTypes.put("multiinstance-state", MultiProcessState.class);
        nodeTypes.put("send-message", SendMessage.class);
        nodeTypes.put("receive-message", ReceiveMessage.class);
    }

    public JpdlXmlReader(byte[] definitionXml) {
        this.definitionXml = definitionXml;
    }

    public ProcessDefinition readProcessDefinition(ProcessDefinition processDefinition) {
        try {
            document = XmlUtils.parseWithXSDValidation(definitionXml, getClass().getResourceAsStream("/jpdl-3.2.xsd"));
            Element root = document.getRootElement();

            // read the process name
            processDefinition.setName(root.attributeValue("name"));
            processDefinition.setDescription(root.elementTextTrim("description"));
            defaultDueDate = root.attributeValue("default-task-duedate");
            if ("true".equals(root.attributeValue("invalid"))) {
                throw new InvalidDefinitionException("invalid process definition");
            }

            // 1: read most content
            readSwimlanes(processDefinition, root);
            readNodes(processDefinition, root);
            readEvents(processDefinition, root, processDefinition);

            // 2: processing transitions
            resolveTransitionDestinations(processDefinition);

            // 3: verify
            verifyElements(processDefinition);
        } catch (Exception e) {
            throw new InvalidDefinitionException(IFileDataProvider.PROCESSDEFINITION_XML_FILE_NAME, e);
        }
        return processDefinition;
    }

	private void readSwimlanes(ProcessDefinition processDefinition, Element processDefinitionElement) {
        List<Element> elements = processDefinitionElement.elements("swimlane");
        for (Element element : elements) {
            String swimlaneName = element.attributeValue("name");
            if (swimlaneName == null) {
                throw new InvalidDefinitionException("there's a swimlane without a name");
            }
            SwimlaneDefinition swimlaneDefinition = new SwimlaneDefinition();
            swimlaneDefinition.setName(swimlaneName);
            Element assignmentElement = element.element("assignment");
            if (assignmentElement != null) {
                swimlaneDefinition.setDelegation(readDelegation(processDefinition, assignmentElement));
            }
            processDefinition.addSwimlane(swimlaneDefinition);
        }
    }

    private void readNodes(ProcessDefinition processDefinition, Element parentElement) {
        List<Element> elements = parentElement.elements();
        for (Element element : elements) {
            String nodeName = element.getName();
            try {
                if (nodeTypes.containsKey(nodeName)) {
                    Node node = ApplicationContextFactory.createAutowiredBean(nodeTypes.get(nodeName));
                    node.setProcessDefinition(processDefinition);
                    readNode(processDefinition, element, node);
                }
            } catch (Exception e) {
                throw new InvalidDefinitionException("couldn't instantiate node '" + nodeName + "'", e);
            }
        }
    }

    private void readTasks(ProcessDefinition processDefinition, Element parentElement, TaskNode taskNode) throws Exception {
        List<Element> elements = parentElement.elements("task");
        for (Element element : elements) {
            readTask(processDefinition, element, taskNode);
        }
    }

    private void readTask(ProcessDefinition processDefinition, Element element, InteractionNode node) throws Exception {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProcessDefinition(processDefinition);
        // get the task name
        String name = element.attributeValue("name");
        if (name != null) {
            taskDefinition.setName(name);
        } else {
            taskDefinition.setName(node.getName());
        }
        // get the task description
        String description = element.elementTextTrim("description");
        if (description != null) {
            taskDefinition.setDescription(description);
        } else {
            taskDefinition.setDescription(node.getDescription());
        }
        // parse common subelements
        readNodeTimers(processDefinition, element, taskDefinition);
        readEvents(processDefinition, element, taskDefinition);
        taskDefinition.setDeadlineDuration(element.attributeValue("duedate", defaultDueDate));
        node.addTask(taskDefinition);
        // assignment
        String swimlaneName = element.attributeValue("swimlane");
        if (swimlaneName == null) {
            if (waitStateCompatibility) {
                processDefinition.getNodes().remove(node);
                WaitState waitState = new WaitState();
                waitState.setProcessDefinition(processDefinition);
                readNode(processDefinition, element.getParent(), waitState);
                return;
            } else {
                throw new InvalidDefinitionException("process xml information: no swimlane or assignment specified for task '"
                        + taskDefinition.getName() + "'");
            }
        } else {
            SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
            taskDefinition.setSwimlane(swimlaneDefinition);
            String reassign = element.attributeValue("reassign");
            if (reassign != null) {
                // if there is a reassign attribute specified
                taskDefinition.setReassignSwimlane(Boolean.valueOf(reassign));
            }
        }
    }

    private List<VariableMapping> readVariableMappings(Element parentElement) {
        List<VariableMapping> variableAccesses = Lists.newArrayList();
        List<Element> elements = parentElement.elements("variable");
        for (Element element : elements) {
            String variableName = element.attributeValue("name");
            if (variableName == null) {
                throw new InvalidDefinitionException("the name attribute of a variable element is required: " + element.asXML());
            }
            String mappedName = element.attributeValue("mapped-name");
            if (mappedName == null) {
                throw new InvalidDefinitionException("the mapped-name attribute of a variable element is required: " + element.asXML());
            }
            String access = element.attributeValue("access", "read,write");
            variableAccesses.add(new VariableMapping(variableName, mappedName, access));
        }
        return variableAccesses;
    }

    private void readNode(ProcessDefinition processDefinition, Element element, Node node) throws Exception {
        node.setName(element.attributeValue("name"));
        node.setDescription(element.elementTextTrim("description"));
        processDefinition.addNode(node);
        readEvents(processDefinition, element, node);
        readNodeTimers(processDefinition, element, node);
        // save the transitions and parse them at the end
        addUnresolvedTransitionDestination(element, node);

        if (node instanceof StartState) {
            StartState startState = (StartState) node;
            Element startTaskElement = element.element("task");
            if (startTaskElement != null) {
                readTask(processDefinition, startTaskElement, startState);
            }
        }
        if (node instanceof ActionNode) {
            ActionNode actionNode = (ActionNode) node;
            actionNode.setAction(readSingleAction(processDefinition, element));
        }
        // if (node instanceof WaitState) {
        // CreateTimerAction createTimerAction = ApplicationContextFactory.createAutowiredBean(CreateTimerAction.class);
        // createTimerAction.setName(node.getName() + "/wait");
        // createTimerAction.setTransitionName(element.attributeValue("transition"));
        // createTimerAction.setDueDate(element.attributeValue("duedate"));
        // addAction(node, Event.EVENTTYPE_NODE_ENTER, createTimerAction);
        // }
        if (node instanceof VariableContainerNode) {
            VariableContainerNode variableContainerNode = (VariableContainerNode) node;
            variableContainerNode.setVariableMappings(readVariableMappings(element));
        }
        if (node instanceof EndNode) {
            EndNode endNode = (EndNode) node;
            endNode.setEndCompleteProcess(Boolean.valueOf(element.attributeValue("end-complete-process", "false")));
        }
        if (node instanceof TaskNode) {
            TaskNode taskNode = (TaskNode) node;
            taskNode.setSignal(TaskNode.parseSignal(element.attributeValue("signal", "first")));
            taskNode.setEndTasks(Boolean.valueOf(element.attributeValue("end-tasks", "true")));
            readTasks(processDefinition, element, taskNode);
        }
        if (node instanceof SubProcessState) {
            SubProcessState subProcessState = (SubProcessState) node;
            Element subProcessElement = element.element("sub-process");
            if (subProcessElement != null) {
                subProcessState.setSubProcessName(subProcessElement.attributeValue("name"));
            }
        }
        if (node instanceof Decision) {
            Decision decision = (Decision) node;
            Element decisionHandlerElement = element.element("handler");
            if (decisionHandlerElement == null) {
                throw new InvalidDefinitionException("No handler in decision found: " + node.getName());
            }
            decision.setDelegation(readDelegation(processDefinition, decisionHandlerElement));
        }
    }

    private void readNodeTimers(ProcessDefinition processDefinition, Element parentElement, GraphElement node) throws Exception {
        List<Element> elements = parentElement.elements("timer");
        int timerNumber = 1;
        for (Element element : elements) {
            String name = node.getNodeId() + "/timer-" + (timerNumber++);
            CreateTimerAction createTimerAction = ApplicationContextFactory.createAutowiredBean(CreateTimerAction.class);
            createTimerAction.setName(name);
            createTimerAction.setTransitionName(element.attributeValue("transition"));
            createTimerAction.setDueDate(element.attributeValue("duedate"));
            createTimerAction.setRepeatDurationString(element.attributeValue("repeat"));
            if (node instanceof TaskDefinition) {
                throw new UnsupportedOperationException("task/timer");
            }
            String createEventType = node instanceof TaskNode ? Event.EVENTTYPE_TASK_CREATE : Event.EVENTTYPE_NODE_ENTER;
            addAction(node, createEventType, createTimerAction);
            Action timerAction = readSingleAction(processDefinition, element);
            if (timerAction != null) {
                timerAction.setName(name);
                addAction(node, Event.EVENTTYPE_TIMER, timerAction);
            }

            CancelTimerAction cancelTimerAction = ApplicationContextFactory.createAutowiredBean(CancelTimerAction.class);
            cancelTimerAction.setName(name);
            String cancelEventType = node instanceof TaskDefinition ? Event.EVENTTYPE_TASK_END : Event.EVENTTYPE_NODE_LEAVE;
            addAction(node, cancelEventType, cancelTimerAction);
        }
    }

    private void readEvents(ProcessDefinition processDefinition, Element parentElement, GraphElement graphElement) {
        List<Element> elements = parentElement.elements("event");
        for (Element eventElement : elements) {
            String eventType = eventElement.attributeValue("type");
            readActions(processDefinition, eventElement, graphElement, eventType);
        }
    }

    private void readActions(ProcessDefinition processDefinition, Element eventElement, GraphElement graphElement, String eventType) {
        // for all the elements in the event element
        List<Element> elements = eventElement.elements();
        for (Element actionElement : elements) {
            Action action = createAction(processDefinition, actionElement);
            addAction(graphElement, eventType, action);
        }
    }

    private void addAction(GraphElement graphElement, String eventType, Action action) {
        Event event = graphElement.getEvent(eventType);
        if (event == null) {
            event = new Event(eventType);
            graphElement.addEvent(event);
        }
        action.setParent(graphElement);
        event.addAction(action);
    }

    private Action readSingleAction(ProcessDefinition processDefinition, Element nodeElement) {
        Action action = null;
        // search for the first action element in the node
        Iterator iter = nodeElement.elementIterator();
        while (iter.hasNext() && (action == null)) {
            Element candidate = (Element) iter.next();
            // parse the action and assign it to this node
            action = createAction(processDefinition, candidate);
        }
        return action;
    }

    private Action createAction(ProcessDefinition processDefinition, Element element) {
        String actionName = element.getName();
        try {
            Action action = new Action();
            action.setName(element.attributeValue("name"));
            action.setDelegation(readDelegation(processDefinition, element));
            String acceptPropagatedEvents = element.attributeValue("accept-propagated-events");
            if ("false".equalsIgnoreCase(acceptPropagatedEvents)) {
                action.setPropagationAllowed(false);
            }
            return action;
        } catch (Exception e) {
            throw new InvalidDefinitionException("couldn't create action '" + actionName + "'", e);
        }
    }

    private Delegation readDelegation(ProcessDefinition processDefinition, Element element) {
        String className = element.attributeValue("class");
        if (className == null) {
            throw new InvalidDefinitionException("no className specified in " + element.asXML());
        }
        String configuration;
        if (element.hasContent()) {
            try {
                List<Node> nodes = element.content();
                if (nodes.size() == 1 && nodes.get(0) instanceof CDATA) {
                    CDATA cdata = (CDATA) nodes.get(0);
                    configuration = cdata.getText();
                    configuration = configuration.trim();
                } else {
                    StringWriter stringWriter = new StringWriter();
                    // when parsing, it could be to store the config in the database, so we want to make the configuration compact
                    XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createCompactFormat());
                    for (Node node : nodes) {
                        xmlWriter.write(node);
                    }
                    xmlWriter.flush();
                    configuration = stringWriter.toString();
                }
            } catch (IOException e) {
                throw new InvalidDefinitionException("io problem while parsing the configuration of " + element.asXML());
            }
        } else {
            configuration = "";
        }
        return new Delegation(className, configuration);
    }

    // transition destinations are parsed in a second pass
    // //////////////////////

    private void addUnresolvedTransitionDestination(Element nodeElement, Node node) {
        unresolvedTransitionDestinations.add(new Object[] { nodeElement, node });
    }

    private void resolveTransitionDestinations(ProcessDefinition processDefinition) {
        for (Object[] unresolvedTransition : unresolvedTransitionDestinations) {
            Element nodeElement = (Element) unresolvedTransition[0];
            Node node = (Node) unresolvedTransition[1];
            List<Element> transitionElements = nodeElement.elements("transition");
            for (Element transitionElement : transitionElements) {
                resolveTransitionDestination(processDefinition, transitionElement, node);
            }
        }
    }

    /**
     * creates the transition object and configures it by the read attributes
     * 
     * @return the created <code>ru.runa.wfe.lang.Transition</code> object (useful, if you want to override this method to read additional configuration properties)
     */
    private void resolveTransitionDestination(ProcessDefinition processDefinition, Element element, Node node) {
        Transition transition = new Transition();
        transition.setProcessDefinition(processDefinition);
        // add the transition to the node
        node.addLeavingTransition(transition);
        transition.setName(element.attributeValue("name"));
        transition.setDescription(element.elementTextTrim("description"));
        // set destinationNode of the transition
        String toName = element.attributeValue("to");
        if (toName == null) {
            throw new InvalidDefinitionException("node '" + node + "' has a transition without a 'to'-attribute");
        }
        Node to = processDefinition.getNodeNotNull(toName);
        to.addArrivingTransition(transition);
        // read the actions
        readActions(processDefinition, element, transition, Event.EVENTTYPE_TRANSITION);
    }

    private void verifyElements(ProcessDefinition processDefinition) {
        for (SwimlaneDefinition swimlaneDefinition : processDefinition.getSwimlanes().values()) {
            SwimlaneDefinition startTaskSwimlane = processDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
            if (swimlaneDefinition.getDelegation() == null && swimlaneDefinition != startTaskSwimlane) {
                throw new InvalidDefinitionException("swimlane '" + swimlaneDefinition.getName() + "' does not have an assignment");
            }
        }
        for (Node node : processDefinition.getNodes()) {
            try {
                node.validate();
            } catch (Exception e) {
                throw new InvalidDefinitionException("validation fails", e);
            }
        }
    }
}

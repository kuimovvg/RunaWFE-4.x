package ru.runa.wfe.definition.jpdl;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.logic.SwimlaneUtils;
import ru.runa.wfe.job.CancelTimerAction;
import ru.runa.wfe.job.CreateTimerAction;
import ru.runa.wfe.job.Timer;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.Decision;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.EndNode;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.Fork;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.MultiProcessState;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.ReceiveMessage;
import ru.runa.wfe.lang.SendMessage;
import ru.runa.wfe.lang.ServiceTask;
import ru.runa.wfe.lang.StartState;
import ru.runa.wfe.lang.SubProcessState;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskExecutionMode;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.VariableContainerNode;
import ru.runa.wfe.lang.WaitState;
import ru.runa.wfe.lang.jpdl.Join;
import ru.runa.wfe.lang.jpdl.JpdlEndTokenNode;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked" })
public class JpdlXmlReader {
    private String defaultDueDate;
    private final List<Object[]> unresolvedTransitionDestinations = Lists.newArrayList();

    @Autowired
    private LocalizationDAO localizationDAO;

    private final Document document;
    // TODO move to Spring (or GPD process setting)
    private boolean waitStateCompatibility = true;

    private static final String INVALID_ATTR = "invalid";
    private static final String ACCESS_ATTR = "access";
    private static final String VARIABLE_NODE = "variable";
    private static final String SUB_PROCESS_NODE = "sub-process";
    private static final String MAPPED_NAME_ATTR = "mapped-name";
    private static final String DUEDATE_ATTR = "duedate";
    private static final String DEFAULT_DUEDATE_ATTR = "default-task-duedate";
    private static final String REPEAT_ATTR = "repeat";
    private static final String TIMER_NODE = "timer";
    private static final String ASSIGNMENT_NODE = "assignment";
    private static final String ID_ATTR = "id";
    private static final String SWIMLANE_ATTR = "swimlane";
    private static final String TRANSITION_ATTR = "transition";
    private static final String TASK_NODE = "task";
    private static final String SWIMLANE_NODE = "swimlane";
    private static final String REASSIGN_ATTR = "reassign";
    private static final String TO_ATTR = "to";
    private static final String CLASS_ATTR = "class";
    private static final String EVENT_NODE = "event";
    private static final String TRANSITION_NODE = "transition";
    private static final String HANDLER_NODE = "handler";
    private static final String DESCRIPTION_NODE = "description";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";
    private static final String ASYNC_ATTR = "async";
    private static final String TASK_EXECUTORS_ATTR = "taskExecutors";
    private static final String TASK_EXECUTION_MODE_ATTR = "taskExecutionMode";
    private static final String ACTION_NODE = "action";

    private static Map<String, Class<? extends Node>> nodeTypes = Maps.newHashMap();
    static {
        nodeTypes.put("start-state", StartState.class);
        nodeTypes.put("end-token-state", JpdlEndTokenNode.class);
        nodeTypes.put("end-state", EndNode.class);
        nodeTypes.put("wait-state", WaitState.class);
        nodeTypes.put("task-node", TaskNode.class);
        nodeTypes.put("multi-task-node", MultiTaskNode.class);
        nodeTypes.put("fork", Fork.class);
        nodeTypes.put("join", Join.class);
        nodeTypes.put("decision", Decision.class);
        nodeTypes.put("process-state", SubProcessState.class);
        nodeTypes.put("multiinstance-state", MultiProcessState.class);
        nodeTypes.put("send-message", SendMessage.class);
        nodeTypes.put("receive-message", ReceiveMessage.class);
        nodeTypes.put("node", ServiceTask.class);
    }

    public JpdlXmlReader(Document document) {
        this.document = document;
    }

    public ProcessDefinition readProcessDefinition(ProcessDefinition processDefinition) {
        try {
            // TODO document = XmlUtils.parseWithXSDValidation(definitionXml,
            // ClassLoaderUtil.getResourceAsStream("jpdl-4.0.xsd", getClass()));
            Element root = document.getRootElement();

            // read the process name
            processDefinition.setName(root.attributeValue(NAME_ATTR));
            processDefinition.setDescription(root.elementTextTrim(DESCRIPTION_NODE));
            defaultDueDate = root.attributeValue(DEFAULT_DUEDATE_ATTR);
            if ("true".equals(root.attributeValue(INVALID_ATTR))) {
                throw new InvalidDefinitionException(processDefinition.getName(), "invalid process definition");
            }

            // 1: read most content
            readSwimlanes(processDefinition, root);
            readNodes(processDefinition, root);
            readEvents(processDefinition, root, processDefinition);

            // 2: processing transitions
            resolveTransitionDestinations(processDefinition);

            // 3: verify
            verifyElements(processDefinition);
        } catch (Throwable th) {
            Throwables.propagateIfInstanceOf(th, InvalidDefinitionException.class);
            throw new InvalidDefinitionException(processDefinition.getName(), th);
        }
        return processDefinition;
    }

    private void readSwimlanes(ProcessDefinition processDefinition, Element processDefinitionElement) {
        List<Element> elements = processDefinitionElement.elements(SWIMLANE_NODE);
        for (Element element : elements) {
            String swimlaneName = element.attributeValue(NAME_ATTR);
            if (swimlaneName == null) {
                throw new InvalidDefinitionException(processDefinition.getName(), "there's a swimlane without a name");
            }
            SwimlaneDefinition swimlaneDefinition = new SwimlaneDefinition();
            swimlaneDefinition.setName(swimlaneName);
            Element assignmentElement = element.element(ASSIGNMENT_NODE);
            if (assignmentElement != null) {
                swimlaneDefinition.setDelegation(readDelegation(processDefinition, assignmentElement));
            }
            SwimlaneUtils.setOrgFunctionLabel(swimlaneDefinition, localizationDAO);
            processDefinition.addSwimlane(swimlaneDefinition);
        }
    }

    private void readNodes(ProcessDefinition processDefinition, Element parentElement) {
        List<Element> elements = parentElement.elements();
        for (Element element : elements) {
            String nodeName = element.getName();
            if (nodeTypes.containsKey(nodeName)) {
                Node node = ApplicationContextFactory.createAutowiredBean(nodeTypes.get(nodeName));
                node.setProcessDefinition(processDefinition);
                readNode(processDefinition, element, node);
            }
        }
    }

    private void readTasks(ProcessDefinition processDefinition, Element parentElement, InteractionNode taskNode) {
        List<Element> elements = parentElement.elements(TASK_NODE);
        for (Element element : elements) {
            readTask(processDefinition, element, taskNode);
        }
    }

    private void readTask(ProcessDefinition processDefinition, Element element, InteractionNode node) {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setNodeId(node.getNodeId());
        taskDefinition.setProcessDefinition(processDefinition);
        // get the task name
        String name = element.attributeValue(NAME_ATTR);
        if (name != null) {
            taskDefinition.setName(name);
        } else {
            taskDefinition.setName(node.getName());
        }
        // get the task description
        String description = element.elementTextTrim(DESCRIPTION_NODE);
        if (description != null) {
            taskDefinition.setDescription(description);
        } else {
            taskDefinition.setDescription(node.getDescription());
        }
        // parse common subelements
        readNodeTimers(processDefinition, element, taskDefinition);
        readEvents(processDefinition, element, taskDefinition);
        taskDefinition.setDeadlineDuration(element.attributeValue(DUEDATE_ATTR, defaultDueDate));
        node.addTask(taskDefinition);
        // assignment
        String swimlaneName = element.attributeValue(SWIMLANE_ATTR);
        if (swimlaneName == null) {
            if (node instanceof MultiTaskNode) {
            } else if (waitStateCompatibility) {
                processDefinition.getNodes().remove(node);
                WaitState waitState = new WaitState();
                waitState.setProcessDefinition(processDefinition);
                readNode(processDefinition, element.getParent(), waitState);
                return;
            } else {
                throw new InvalidDefinitionException(processDefinition.getName(),
                        "process xml information: no swimlane or assignment specified for task '" + taskDefinition + "'");
            }
        } else {
            SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
            taskDefinition.setSwimlane(swimlaneDefinition);
            String reassign = element.attributeValue(REASSIGN_ATTR);
            if (reassign != null) {
                // if there is a reassign attribute specified
                taskDefinition.setReassignSwimlane(Boolean.valueOf(reassign));
            }
        }
    }

    private List<VariableMapping> readVariableMappings(ProcessDefinition processDefinition, Element parentElement) {
        List<VariableMapping> variableAccesses = Lists.newArrayList();
        List<Element> elements = parentElement.elements(VARIABLE_NODE);
        for (Element element : elements) {
            String variableName = element.attributeValue(NAME_ATTR);
            if (variableName == null) {
                throw new InvalidDefinitionException(processDefinition.getName(), "the name attribute of a variable element is required: "
                        + element.asXML());
            }
            String mappedName = element.attributeValue(MAPPED_NAME_ATTR);
            if (mappedName == null) {
                throw new InvalidDefinitionException(processDefinition.getName(), "the mapped-name attribute of a variable element is required: "
                        + element.asXML());
            }
            String access = element.attributeValue(ACCESS_ATTR, "read,write");
            variableAccesses.add(new VariableMapping(variableName, mappedName, access));
        }
        return variableAccesses;
    }

    private void readNode(ProcessDefinition processDefinition, Element element, Node node) {
        node.setNodeId(element.attributeValue(ID_ATTR));
        node.setName(element.attributeValue(NAME_ATTR));
        node.setDescription(element.elementTextTrim(DESCRIPTION_NODE));
        processDefinition.addNode(node);
        readEvents(processDefinition, element, node);
        // save the transitions and parse them at the end
        addUnresolvedTransitionDestination(element, node);

        if (node instanceof StartState) {
            StartState startState = (StartState) node;
            Element startTaskElement = element.element(TASK_NODE);
            if (startTaskElement != null) {
                readTask(processDefinition, startTaskElement, startState);
            }
        }
        // if (node instanceof WaitState) {
        // CreateTimerAction createTimerAction =
        // ApplicationContextFactory.createAutowiredBean(CreateTimerAction.class);
        // createTimerAction.setName(node.getName() + "/wait");
        // createTimerAction.setTransitionName(element.attributeValue("transition"));
        // createTimerAction.setDueDate(element.attributeValue("duedate"));
        // addAction(node, Event.EVENTTYPE_NODE_ENTER, createTimerAction);
        // }
        if (node instanceof VariableContainerNode) {
            VariableContainerNode variableContainerNode = (VariableContainerNode) node;
            variableContainerNode.setVariableMappings(readVariableMappings(processDefinition, element));
        }
        if (node instanceof TaskNode) {
            TaskNode taskNode = (TaskNode) node;
            taskNode.setAsync(Boolean.valueOf(element.attributeValue(ASYNC_ATTR, "false")));
            readTasks(processDefinition, element, taskNode);
        }
        if (node instanceof MultiTaskNode) {
            MultiTaskNode multiTaskNode = (MultiTaskNode) node;
            multiTaskNode.setAsync(Boolean.valueOf(element.attributeValue(ASYNC_ATTR, "false")));
            multiTaskNode.setMode(TaskExecutionMode.valueOf(element.attributeValue(TASK_EXECUTION_MODE_ATTR, TaskExecutionMode.last.name())));
            multiTaskNode.setExecutorsVariableName(element.attributeValue(TASK_EXECUTORS_ATTR));
            readTasks(processDefinition, element, multiTaskNode);
        }
        if (node instanceof SubProcessState) {
            SubProcessState subProcessState = (SubProcessState) node;
            Element subProcessElement = element.element(SUB_PROCESS_NODE);
            if (subProcessElement != null) {
                subProcessState.setSubProcessName(subProcessElement.attributeValue(NAME_ATTR));
            }
        }
        if (node instanceof Decision) {
            Decision decision = (Decision) node;
            Element decisionHandlerElement = element.element(HANDLER_NODE);
            if (decisionHandlerElement == null) {
                throw new InvalidDefinitionException(processDefinition.getName(), "No handler in decision found: " + node);
            }
            decision.setDelegation(readDelegation(processDefinition, decisionHandlerElement));
        }
        if (node instanceof SendMessage) {
            SendMessage sendMessage = (SendMessage) node;
            sendMessage.setTtlDuration(element.attributeValue(DUEDATE_ATTR, "1 days"));
        }
        if (node instanceof ServiceTask) {
            ServiceTask serviceTask = (ServiceTask) node;
            Element actionElement = element.element(ACTION_NODE);
            Preconditions.checkNotNull(actionElement, "No action defined in " + serviceTask);
            serviceTask.setDelegation(readDelegation(processDefinition, actionElement));
        }
        readNodeTimers(processDefinition, element, node);
    }

    private void readNodeTimers(ProcessDefinition processDefinition, Element parentElement, GraphElement node) {
        List<Element> elements = parentElement.elements(TIMER_NODE);
        int timerNumber = 1;
        for (Element element : elements) {
            // 1 timer for compatibility timer names with 3.x
            String name;
            if (SystemProperties.isV3CompatibilityMode()) {
                name = element.attributeValue(NAME_ATTR, node.getNodeId());
            } else {
                name = node.getNodeId() + "/timer-" + (timerNumber++);
            }
            CreateTimerAction createTimerAction = ApplicationContextFactory.createAutowiredBean(CreateTimerAction.class);
            createTimerAction.setName(name);
            createTimerAction.setTransitionName(element.attributeValue(TRANSITION_ATTR));
            String durationString = element.attributeValue(DUEDATE_ATTR);
            if (Strings.isNullOrEmpty(durationString) && node instanceof TaskNode && Timer.ESCALATION_NAME.equals(name)) {
                durationString = ((TaskNode) node).getFirstTaskNotNull().getDeadlineDuration();
                if (Strings.isNullOrEmpty(durationString)) {
                    throw new InvalidDefinitionException(processDefinition.getName(), "No '" + DUEDATE_ATTR + "' specified for timer in " + node);
                }
            }
            createTimerAction.setDueDate(durationString);
            createTimerAction.setRepeatDurationString(element.attributeValue(REPEAT_ATTR));
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
        List<Element> elements = parentElement.elements(EVENT_NODE);
        for (Element eventElement : elements) {
            String eventType = eventElement.attributeValue(TYPE_ATTR);
            readActions(processDefinition, eventElement, graphElement, eventType);
        }
    }

    private void readActions(ProcessDefinition processDefinition, Element eventElement, GraphElement graphElement, String eventType) {
        // for all the elements in the event element
        List<Element> elements = eventElement.elements(ACTION_NODE);
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
        Element actionElement = nodeElement.element(ACTION_NODE);
        if (actionElement != null) {
            return createAction(processDefinition, actionElement);
        }
        return null;
    }

    private Action createAction(ProcessDefinition processDefinition, Element element) {
        Action action = new Action();
        action.setName(element.attributeValue(NAME_ATTR));
        action.setDelegation(readDelegation(processDefinition, element));
        return action;
    }

    private Delegation readDelegation(ProcessDefinition processDefinition, Element element) {
        String className = element.attributeValue(CLASS_ATTR);
        if (className == null) {
            throw new InvalidDefinitionException(processDefinition.getName(), "no className specified in " + element.asXML());
        }
        String configuration = element.getText().trim();
        Delegation delegation = new Delegation(className, configuration);
        // check
        try {
            delegation.getInstance();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return delegation;
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
            List<Element> transitionElements = nodeElement.elements(TRANSITION_NODE);
            for (Element transitionElement : transitionElements) {
                resolveTransitionDestination(processDefinition, transitionElement, node);
            }
        }
    }

    /**
     * creates the transition object and configures it by the read attributes
     * 
     * @return the created <code>ru.runa.wfe.lang.Transition</code> object
     *         (useful, if you want to override this method to read additional
     *         configuration properties)
     */
    private void resolveTransitionDestination(ProcessDefinition processDefinition, Element element, Node node) {
        Transition transition = new Transition();
        transition.setProcessDefinition(processDefinition);
        node.addLeavingTransition(transition);
        transition.setName(element.attributeValue(NAME_ATTR));
        for (CreateTimerAction createTimerAction : node.getTimerActions()) {
            if (Objects.equal(createTimerAction.getTransitionName(), transition.getName())) {
                transition.setTimerTransition(true);
            }
        }
        transition.setDescription(element.elementTextTrim(DESCRIPTION_NODE));
        // set destinationNode of the transition
        String toId = element.attributeValue(TO_ATTR);
        if (toId == null) {
            throw new InvalidDefinitionException(processDefinition.getName(), "node '" + node + "' has a transition without a 'to'-attribute");
        }
        Node to = processDefinition.getNodeNotNull(toId);
        to.addArrivingTransition(transition);
        // read the actions
        readActions(processDefinition, element, transition, Event.EVENTTYPE_TRANSITION);
    }

    private void verifyElements(ProcessDefinition processDefinition) {
        for (Node node : processDefinition.getNodes()) {
            node.validate();
        }
    }
}

package ru.runa.wfe.definition.bpmn;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
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
import ru.runa.wfe.lang.EndTokenNode;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.Fork;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.MultiProcessState;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.ReceiveMessage;
import ru.runa.wfe.lang.SendMessage;
import ru.runa.wfe.lang.ServiceTask;
import ru.runa.wfe.lang.StartState;
import ru.runa.wfe.lang.SubProcessState;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.VariableContainerNode;
import ru.runa.wfe.lang.WaitState;
import ru.runa.wfe.lang.bpmn2.ExclusiveDecision;
import ru.runa.wfe.lang.bpmn2.ExclusiveMerge;
import ru.runa.wfe.lang.bpmn2.Join;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked" })
public class BpmnXmlReader {
    private static final String BPMN_PREFIX = "";
    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String RUNA_PREFIX = "runa";
    private static final String RUNA_NAMESPACE = "http://runa.ru/wfe/xml";
    private static final String DEFINITIONS = "definitions";
    private static final String PROCESS = "process";
    private static final String EXTENSION_ELEMENTS = "extensionElements";
    private static final String EXECUTABLE = "isExecutable";
    private static final String PROPERTY = "property";
    private static final String END_STATE = "endEvent";
    private static final String TEXT_ANNOTATION = "textAnnotation";
    private static final String SERVICE_TASK = "serviceTask";
    private static final String TEXT = "text";
    private static final String END_TOKEN_STATE = "endPoint";
    private static final String IO_SPECIFICATION = "ioSpecification";
    private static final String DATA_INPUT = "dataInput";
    private static final String DATA_OUTPUT = "dataOutput";
    private static final String INPUT_SET = "inputSet";
    private static final String OUTPUT_SET = "outputSet";
    private static final String DATA_INPUT_REFS = "dataInputRefs";
    private static final String DATA_OUTPUT_REFS = "dataOutputRefs";
    private static final String DATA_INPUT_ASSOCIATION = "dataInputAssociation";
    private static final String DATA_OUTPUT_ASSOCIATION = "dataOutputAssociation";
    private static final String VARIABLES = "variables";
    private static final String VARIABLE = "variable";
    private static final String SOURCE_REF = "sourceRef";
    private static final String TARGET_REF = "targetRef";
    private static final String SUBPROCESS = "subProcess";
    private static final String MULTI_SUBPROCESS = "multiProcess";
    private static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
    private static final String PARALLEL_GATEWAY = "parallelGateway";
    private static final String DEFAULT_TASK_TIMOUT = "default-task-timeout";
    private static final String REPEAT = "repeat";//
    private static final String USER_TASK = "userTask";
    private static final String START_EVENT = "startEvent";
    private static final String SWIMLANE_SET = "laneSet";
    private static final String SWIMLANE = "lane";
    private static final String FLOW_NODE_REF = "flowNodeRef";
    public static final String SWIMLANE_DISPLAY_MODE = "showSwimlane";
    private static final String REASSIGN = "reassign";//
    private static final String CLASS = "class";
    private static final String SEQUENCE_FLOW = "sequenceFlow";
    private static final String DOCUMENTATION = "documentation";
    private static final String CONFIG = "config";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String MAPPED_NAME = "mappedName";
    private static final String USAGE = "usage";
    private static final String ID = "id";
    private static final String SEND_MESSAGE = "sendTask";
    private static final String RECEIVE_MESSAGE = "receiveTask";
    private static final String BOUNDARY_EVENT = "boundaryEvent";
    private static final String INTERMEDIATE_EVENT = "intermediateCatchEvent";
    private static final String CANCEL_ACTIVITY = "cancelActivity";
    private static final String ATTACHED_TO_REF = "attachedToRef";
    private static final String TIMER_EVENT = "timerEventDefinition";
    private static final String TIMER_DURATION = "timeDuration";

    @Autowired
    private LocalizationDAO localizationDAO;

    private final Document document;

    private static Map<String, Class<? extends Node>> nodeTypes = Maps.newHashMap();
    static {
        nodeTypes.put(START_EVENT, StartState.class);
        nodeTypes.put(USER_TASK, TaskNode.class);
        nodeTypes.put(END_TOKEN_STATE, EndTokenNode.class);
        nodeTypes.put(END_STATE, EndNode.class);
        nodeTypes.put(INTERMEDIATE_EVENT, WaitState.class);
        nodeTypes.put(SUBPROCESS, SubProcessState.class);
        nodeTypes.put(MULTI_SUBPROCESS, MultiProcessState.class);
        nodeTypes.put(SEND_MESSAGE, SendMessage.class);
        nodeTypes.put(RECEIVE_MESSAGE, ReceiveMessage.class);
        nodeTypes.put(SERVICE_TASK, ServiceTask.class);
    }

    public BpmnXmlReader(Document document) {
        this.document = document;
    }

    public ProcessDefinition readProcessDefinition(ProcessDefinition processDefinition) {
        try {
            Element definitionsElement = document.getRootElement();
            Element process = definitionsElement.element(PROCESS);
            processDefinition.setName(process.attributeValue(NAME));
            processDefinition.setDescription(process.elementTextTrim(DOCUMENTATION));
            Map<String, String> processProperties = parseExtensionProperties(process);
            String defaultTaskTimeout = processProperties.get(DEFAULT_TASK_TIMOUT);
            if (!Strings.isNullOrEmpty(defaultTaskTimeout)) {
                // processDefinition.setDefaultTaskTimeoutDelay(new
                // Delay(defaultTaskTimeout));
            }
            String swimlaneDisplayModeName = processProperties.get(SWIMLANE_DISPLAY_MODE);
            if (swimlaneDisplayModeName != null) {
                // definition.setSwimlaneDisplayMode(SwimlaneDisplayMode.valueOf(swimlaneDisplayModeName));
            }
            if ("false".equals(process.attributeValue(EXECUTABLE))) {
                throw new InvalidDefinitionException(processDefinition.getName(), "process is not executable");
            }

            // 1: read most content
            readSwimlanes(processDefinition, process);
            readNodes(processDefinition, process);

            // 2: processing transitions
            readTransitions(processDefinition, process);

            // 3: verify
            verifyElements(processDefinition);
        } catch (Exception e) {
            throw new InvalidDefinitionException(processDefinition.getName(), e);
        }
        return processDefinition;
    }

    private void readSwimlanes(ProcessDefinition processDefinition, Element processElement) {
        Element swimlaneSetElement = processElement.element(SWIMLANE_SET);
        if (swimlaneSetElement != null) {
            List<Element> swimlanes = swimlaneSetElement.elements(SWIMLANE);
            for (Element swimlaneElement : swimlanes) {
                String swimlaneName = swimlaneElement.attributeValue(NAME);
                if (swimlaneName == null) {
                    throw new InternalApplicationException("there's a swimlane without a name");
                }
                SwimlaneDefinition swimlaneDefinition = new SwimlaneDefinition();
                swimlaneDefinition.setName(swimlaneName);
                swimlaneDefinition.setDelegation(readDelegation(swimlaneElement));
                SwimlaneUtils.setOrgFunctionLabel(swimlaneDefinition, localizationDAO);
                List<Element> flowNodeRefElements = swimlaneElement.elements(FLOW_NODE_REF);
                List<String> flowNodeIds = Lists.newArrayList();
                for (Element flowNodeRefElement : flowNodeRefElements) {
                    flowNodeIds.add(flowNodeRefElement.getTextTrim());
                }
                swimlaneDefinition.setFlowNodeIds(flowNodeIds);
                processDefinition.addSwimlane(swimlaneDefinition);
            }
        }
    }

    private void readNodes(ProcessDefinition processDefinition, Element parentElement) {
        List<Element> elements = parentElement.elements();
        for (Element element : elements) {
            String nodeName = element.getName();
            Node node = null;
            if (nodeTypes.containsKey(nodeName)) {
                node = ApplicationContextFactory.createAutowiredBean(nodeTypes.get(nodeName));
            } else if (PARALLEL_GATEWAY.equals(nodeName)) {
                String nodeId = element.attributeValue(ID);
                int outgoingTransitionsCount = getOutgoingTransitionsCount(parentElement, nodeId);
                if (outgoingTransitionsCount > 1) {
                    node = ApplicationContextFactory.createAutowiredBean(Fork.class);
                } else {
                    node = ApplicationContextFactory.createAutowiredBean(Join.class);
                }
            } else if (EXCLUSIVE_GATEWAY.equals(nodeName)) {
                String nodeId = element.attributeValue(ID);
                int outgoingTransitionsCount = getOutgoingTransitionsCount(parentElement, nodeId);
                if (outgoingTransitionsCount > 1) {
                    node = ApplicationContextFactory.createAutowiredBean(ExclusiveDecision.class);
                } else {
                    node = ApplicationContextFactory.createAutowiredBean(ExclusiveMerge.class);
                }
            }
            if (node != null) {
                node.setProcessDefinition(processDefinition);
                readNode(processDefinition, element, node);
            }
        }
    }

    private void readNode(ProcessDefinition processDefinition, Element element, Node node) {
        node.setNodeId(element.attributeValue(ID));
        node.setName(element.attributeValue(NAME));
        node.setDescription(element.elementTextTrim(DOCUMENTATION));
        processDefinition.addNode(node);

        if (node instanceof StartState) {
            StartState startState = (StartState) node;
            readTask(processDefinition, element, startState);
        }
        if (node instanceof TaskNode) {
            TaskNode taskNode = (TaskNode) node;
            readTask(processDefinition, element, taskNode);
            List<Element> boundaryEventElements = element.getParent().elements(BOUNDARY_EVENT);
            for (Element boundaryEventElement : boundaryEventElements) {
                String parentNodeId = boundaryEventElement.attributeValue(ATTACHED_TO_REF);
                if (Objects.equal(parentNodeId, taskNode.getNodeId())) {
                    readTimer(processDefinition, boundaryEventElement, taskNode);
                }
            }
        }
        if (node instanceof VariableContainerNode) {
            VariableContainerNode variableContainerNode = (VariableContainerNode) node;
            variableContainerNode.setVariableMappings(readVariableMappings(element));
        }
        if (node instanceof SubProcessState) {
            SubProcessState subprocess = (SubProcessState) node;
            subprocess.setSubProcessName(element.attributeValue(QName.get(PROCESS, RUNA_NAMESPACE)));
        }
        if (node instanceof Decision) {
            Decision decision = (Decision) node;
            decision.setDelegation(readDelegation(element));
        }
        if (node instanceof WaitState) {
            WaitState waitState = (WaitState) node;
            readTimer(processDefinition, element, waitState);
        }
        if (node instanceof ServiceTask) {
            ServiceTask serviceTask = (ServiceTask) node;
            serviceTask.setDelegation(readDelegation(element));
        }
        if (node instanceof SendMessage) {
            SendMessage sendMessage = (SendMessage) node;
            sendMessage.setTtlDuration(element.attributeValue(QName.get(TIMER_DURATION, RUNA_NAMESPACE), "1 days"));
        }
    }

    private void readTimer(ProcessDefinition processDefinition, Element eventElement, GraphElement node) {
        Element timerElement = eventElement.element(TIMER_EVENT);
        CreateTimerAction createTimerAction = ApplicationContextFactory.createAutowiredBean(CreateTimerAction.class);
        createTimerAction.setNodeId(eventElement.attributeValue(ID));
        String name = eventElement.attributeValue(NAME, node.getNodeId());
        createTimerAction.setName(name);
        String durationString = timerElement.elementTextTrim(TIMER_DURATION);
        if (Strings.isNullOrEmpty(durationString) && node instanceof TaskNode && Timer.ESCALATION_NAME.equals(name)) {
            durationString = ((TaskNode) node).getFirstTaskNotNull().getDeadlineDuration();
            if (Strings.isNullOrEmpty(durationString)) {
                throw new InternalApplicationException("No '" + TIMER_DURATION + "' specified for timer in " + node);
            }
        }
        createTimerAction.setDueDate(durationString);
        // createTimerAction.setRepeatDurationString(element.attributeValue(REPEAT_ATTR));
        String createEventType = node instanceof TaskNode ? Event.EVENTTYPE_TASK_CREATE : Event.EVENTTYPE_NODE_ENTER;
        addAction(node, createEventType, createTimerAction);
        // Action timerAction = readSingleAction(processDefinition, element);
        // if (timerAction != null) {
        // timerAction.setName(createTimerAction.getName());
        // addAction(node, Event.EVENTTYPE_TIMER, timerAction);
        // }

        CancelTimerAction cancelTimerAction = ApplicationContextFactory.createAutowiredBean(CancelTimerAction.class);
        cancelTimerAction.setName(createTimerAction.getName());
        String cancelEventType = node instanceof TaskDefinition ? Event.EVENTTYPE_TASK_END : Event.EVENTTYPE_NODE_LEAVE;
        addAction(node, cancelEventType, cancelTimerAction);
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

    private Map<String, String> parseExtensionProperties(Element element) {
        Map<String, String> map = Maps.newHashMap();
        Element extensionsElement = element.element(EXTENSION_ELEMENTS);
        if (extensionsElement != null) {
            List<Element> propertyElements = extensionsElement.elements(QName.get(PROPERTY, RUNA_NAMESPACE));
            for (Element propertyElement : propertyElements) {
                String name = propertyElement.attributeValue(NAME);
                String value = propertyElement.attributeValue(VALUE);
                if (value == null) {
                    value = propertyElement.getTextTrim();
                }
                map.put(name, value);
            }
        }
        return map;
    }

    private List<VariableMapping> readVariableMappings(Element element) {
        List<VariableMapping> list = Lists.newArrayList();
        Element extensionsElement = element.element(EXTENSION_ELEMENTS);
        if (extensionsElement != null) {
            Element variablesElement = extensionsElement.element(QName.get(VARIABLES, RUNA_NAMESPACE));
            if (variablesElement != null) {
                List<Element> variableElements = variablesElement.elements(QName.get(VARIABLE, RUNA_NAMESPACE));
                for (Element variableElement : variableElements) {
                    VariableMapping variableMapping = new VariableMapping(variableElement.attributeValue(NAME),
                            variableElement.attributeValue(MAPPED_NAME), variableElement.attributeValue(USAGE));
                    list.add(variableMapping);
                }
            }
        }
        return list;
    }

    private void readTransitions(ProcessDefinition processDefinition, Element processElement) {
        List<Element> elements = processElement.elements(SEQUENCE_FLOW);
        for (Element element : elements) {
            String id = element.attributeValue(ID);
            if (id == null) {
                throw new InternalApplicationException("transition without an '" + ID + "'-attribute");
            }
            String name = element.attributeValue(NAME);
            String from = element.attributeValue(SOURCE_REF);
            if (from == null) {
                throw new InternalApplicationException("transition '" + id + "' without a '" + SOURCE_REF + "'-attribute");
            }
            String to = element.attributeValue(TARGET_REF);
            if (to == null) {
                throw new InternalApplicationException("transition '" + id + "' without a '" + TARGET_REF + "'-attribute");
            }
            Transition transition = new Transition();
            transition.setNodeId(id);
            GraphElement sourceElement = processDefinition.getGraphElementNotNull(from);
            Node source;
            if (sourceElement instanceof Node) {
                source = (Node) sourceElement;
            } else if (sourceElement instanceof CreateTimerAction) {
                CreateTimerAction createTimerAction = (CreateTimerAction) sourceElement;
                createTimerAction.setTransitionName(name);
                source = (Node) createTimerAction.getParent();
                transition.setTimerTransition(true);
            } else {
                throw new InternalApplicationException("Unexpected source element " + sourceElement);
            }
            transition.setFrom(source);
            Node target = processDefinition.getNodeNotNull(to);
            transition.setTo(target);
            transition.setName(name);
            transition.setDescription(element.elementTextTrim(DOCUMENTATION));
            transition.setProcessDefinition(processDefinition);
            // add the transition to the node
            source.addLeavingTransition(transition);
            // set destinationNode of the transition
            target.addArrivingTransition(transition);
            // read the actions
            // readActions(processDefinition, element, transition,
            // Event.EVENTTYPE_TRANSITION);
        }
    }

    private int getOutgoingTransitionsCount(Element processDefinitionElement, String sourceNodeId) {
        int count = 0;
        List<Element> elements = processDefinitionElement.elements(SEQUENCE_FLOW);
        for (Element element : elements) {
            String from = element.attributeValue(SOURCE_REF);
            if (Objects.equal(from, sourceNodeId)) {
                count++;
            }
        }
        return count;
    }

    private void readTask(ProcessDefinition processDefinition, Element element, InteractionNode node) {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setNodeId(node.getNodeId());
        taskDefinition.setProcessDefinition(processDefinition);
        taskDefinition.setName(node.getName());
        taskDefinition.setDescription(node.getDescription());
        node.addTask(taskDefinition);
        // assignment
        Map<String, String> properties = parseExtensionProperties(element);
        String swimlaneName = properties.get(SWIMLANE);
        SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
        taskDefinition.setSwimlane(swimlaneDefinition);
        String reassign = properties.get(REASSIGN);
        if (reassign != null) {
            boolean forceReassign = Boolean.parseBoolean(reassign);
            taskDefinition.setReassignSwimlane(forceReassign);
        }
    }

    private Delegation readDelegation(Element element) {
        Map<String, String> swimlaneProperties = parseExtensionProperties(element);
        String className = swimlaneProperties.get(CLASS);
        if (className == null) {
            throw new InternalApplicationException("no className specified in " + element.asXML());
        }
        ClassLoaderUtil.instantiate(className);
        String configuration = swimlaneProperties.get(CONFIG);
        return new Delegation(className, configuration);
    }

    private void verifyElements(ProcessDefinition processDefinition) {
        for (Node node : processDefinition.getNodes()) {
            node.validate();
        }
    }
}

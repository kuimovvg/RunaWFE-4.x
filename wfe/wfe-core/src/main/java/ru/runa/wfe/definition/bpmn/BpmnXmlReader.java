package ru.runa.wfe.definition.bpmn;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.EndNode;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.StartState;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;

import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked" })
public class BpmnXmlReader {

    @Autowired
    private LocalizationDAO localizationDAO;

    private final Document document;

    private static Map<String, Class<? extends Node>> nodeTypes = Maps.newHashMap();
    static {
        nodeTypes.put("startEvent", StartState.class);
        nodeTypes.put("userTask", TaskNode.class);
        nodeTypes.put("endEvent", EndNode.class);
    }

    public BpmnXmlReader(Document document) {
        this.document = document;
    }

    private static final String INVALID_ATTR = "invalid";
    private static final String ACCESS_ATTR = "access";
    private static final String VARIABLE_NODE = "variable";
    private static final String SUB_PROCESS_NODE = "sub-process";
    private static final String MAPPED_NAME_ATTR = "mapped-name";
    private static final String DECISION_NODE = "decision";
    private static final String DUEDATE_ATTR = "duedate";
    private static final String REPEAT_ATTR = "repeat";
    private static final String TIMER_NODE = "timer";
    private static final String ASSIGNMENT_NODE = "assignment";
    private static final String ID_ATTR = "id";
    private static final String SWIMLANE_ATTR = "swimlane";
    private static final String TRANSITION_ATTR = "transition";
    private static final String TASK_NODE = "task";
    private static final String SWIMLANE_NODE = "swimlane";
    private static final String REASSIGN_ATTR = "reassign";
    private static final String CLASS_ATTR = "class";
    private static final String EVENT_NODE = "event";
    private static final String TRANSITION_NODE = "sequenceFlow";
    private static final String TRANSITION_FROM_ATTR = "sourceRef";
    private static final String TRANSITION_TO_ATTR = "targetRef";
    private static final String HANDLER_NODE = "handler";
    private static final String DESCRIPTION_NODE = "documentation";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";

    public ProcessDefinition readProcessDefinition(ProcessDefinition processDefinition) {
        try {
            Element root = document.getRootElement();
            Element process = root.element("process");

            // read the process name
            processDefinition.setName(process.attributeValue(NAME_ATTR));
            processDefinition.setDescription(process.elementTextTrim(DESCRIPTION_NODE));

            // 1: read most content
            readSwimlanes(processDefinition, process);
            readNodes(processDefinition, process);

            // 2: processing transitions
            readTransitions(processDefinition, process);

            // 3: verify
            verifyElements(processDefinition);
        } catch (Exception e) {
            throw new InvalidDefinitionException(IFileDataProvider.PROCESSDEFINITION_XML_FILE_NAME, e);
        }
        return processDefinition;
    }

    private void readSwimlanes(ProcessDefinition processDefinition, Element processDefinitionElement) {
        List<Element> elements = processDefinitionElement.elements(SWIMLANE_NODE);
        for (Element element : elements) {
            String swimlaneName = element.attributeValue(NAME_ATTR);
            if (swimlaneName == null) {
                throw new InvalidDefinitionException("there's a swimlane without a name");
            }
            SwimlaneDefinition swimlaneDefinition = new SwimlaneDefinition();
            swimlaneDefinition.setName(swimlaneName);
            Element assignmentElement = element.element(ASSIGNMENT_NODE);
            if (assignmentElement != null) {
                swimlaneDefinition.setDelegation(readDelegation(processDefinition, assignmentElement));
            }
            if (swimlaneDefinition.getDelegation() != null && swimlaneDefinition.getDelegation().getConfiguration() != null) {
                String conf = swimlaneDefinition.getDelegation().getConfiguration();
                swimlaneDefinition.setDisplayOrgFunction(conf);
                String[] orgFunctionParts = conf.split("\\(");
                if (orgFunctionParts.length == 2) {
                    String localized = localizationDAO.getLocalized(orgFunctionParts[0].trim());
                    if (localized != null) {
                        swimlaneDefinition.setDisplayOrgFunction(localized + " (" + orgFunctionParts[1]);
                    }
                }
            }
            processDefinition.addSwimlane(swimlaneDefinition);
        }
    }

    private void readTransitions(ProcessDefinition processDefinition, Element processDefinitionElement) {
        List<Element> elements = processDefinitionElement.elements(TRANSITION_NODE);
        for (Element element : elements) {
            String name = element.attributeValue(ID_ATTR);
            if (name == null) {
                throw new InvalidDefinitionException("transition without an '" + ID_ATTR + "'-attribute");
            }
            String from = element.attributeValue(TRANSITION_FROM_ATTR);
            if (from == null) {
                throw new InvalidDefinitionException("transition '" + name + "' without a '" + TRANSITION_FROM_ATTR + "'-attribute");
            }
            String to = element.attributeValue(TRANSITION_TO_ATTR);
            if (to == null) {
                throw new InvalidDefinitionException("transition '" + name + "' without a '" + TRANSITION_TO_ATTR + "'-attribute");
            }
            Transition transition = new Transition();
            Node source = processDefinition.getNodeNotNull(from);
            transition.setFrom(source);
            Node target = processDefinition.getNodeNotNull(to);
            transition.setTo(target);
            transition.setName(name);
            transition.setDescription(element.elementTextTrim(DESCRIPTION_NODE));
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

    private void readTask(ProcessDefinition processDefinition, Element element, InteractionNode node) throws Exception {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setNodeId(node.getNodeId());
        taskDefinition.setProcessDefinition(processDefinition);
        // get the task name
        // String name = element.attributeValue(NAME_ATTR);
        // if (name != null) {
        // taskDefinition.setName(name);
        // } else {
        taskDefinition.setName(node.getName());
        // }
        // get the task description
        // String description = element.elementTextTrim(DESCRIPTION_NODE);
        // if (description != null) {
        // taskDefinition.setDescription(description);
        // } else {
        taskDefinition.setDescription(node.getDescription());
        // }
        // parse common subelements
        // readNodeTimers(processDefinition, element, taskDefinition);
        // readEvents(processDefinition, element, taskDefinition);
        node.addTask(taskDefinition);
        // assignment
        String swimlaneName = element.attributeValue(SWIMLANE_ATTR);
        SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
        taskDefinition.setSwimlane(swimlaneDefinition);
        String reassign = element.attributeValue(REASSIGN_ATTR);
        if (reassign != null) {
            // if there is a reassign attribute specified
            taskDefinition.setReassignSwimlane(Boolean.valueOf(reassign));
        }
    }

    // private List<VariableMapping> readVariableMappings(Element parentElement)
    // {
    // List<VariableMapping> variableAccesses = Lists.newArrayList();
    // List<Element> elements = parentElement.elements(VARIABLE_NODE);
    // for (Element element : elements) {
    // String variableName = element.attributeValue(NAME_ATTR);
    // if (variableName == null) {
    // throw new
    // InvalidDefinitionException("the name attribute of a variable element is required: "
    // + element.asXML());
    // }
    // String mappedName = element.attributeValue(MAPPED_NAME_ATTR);
    // if (mappedName == null) {
    // throw new
    // InvalidDefinitionException("the mapped-name attribute of a variable element is required: "
    // + element.asXML());
    // }
    // String access = element.attributeValue(ACCESS_ATTR, "read,write");
    // variableAccesses.add(new VariableMapping(variableName, mappedName,
    // access));
    // }
    // return variableAccesses;
    // }

    private void readNode(ProcessDefinition processDefinition, Element element, Node node) throws Exception {
        node.setNodeId(element.attributeValue(ID_ATTR));
        node.setName(element.attributeValue(NAME_ATTR));
        node.setDescription(element.elementTextTrim(DESCRIPTION_NODE));
        processDefinition.addNode(node);
        // readEvents(processDefinition, element, node);
        // readNodeTimers(processDefinition, element, node);

        if (node instanceof StartState) {
            StartState startState = (StartState) node;
            readTask(processDefinition, element, startState);
        }
        if (node instanceof EndNode) {
            EndNode endNode = (EndNode) node;
            endNode.setEndCompleteProcess(Boolean.valueOf(element.attributeValue("end-complete-process", "false")));
        }
        if (node instanceof TaskNode) {
            TaskNode taskNode = (TaskNode) node;
            taskNode.setSignal(TaskNode.parseSignal(element.attributeValue("signal", "first")));
            taskNode.setEndTasks(Boolean.valueOf(element.attributeValue("end-tasks", "true")));
            readTask(processDefinition, element, taskNode);
        }
    }

    //
    // private void readNodeTimers(ProcessDefinition processDefinition, Element
    // parentElement, GraphElement node) throws Exception {
    // List<Element> elements = parentElement.elements(TIMER_NODE);
    // int timerNumber = 1;
    // for (Element element : elements) {
    // String name = node.getNodeId() + "/timer-" + (timerNumber++);
    // CreateTimerAction createTimerAction =
    // ApplicationContextFactory.createAutowiredBean(CreateTimerAction.class);
    // createTimerAction.setName(name);
    // createTimerAction.setTransitionName(element.attributeValue(TRANSITION_ATTR));
    // createTimerAction.setDueDate(element.attributeValue(DUEDATE_ATTR));
    // createTimerAction.setRepeatDurationString(element.attributeValue(REPEAT_ATTR));
    // if (node instanceof TaskDefinition) {
    // throw new UnsupportedOperationException("task/timer");
    // }
    // String createEventType = node instanceof TaskNode ?
    // Event.EVENTTYPE_TASK_CREATE : Event.EVENTTYPE_NODE_ENTER;
    // addAction(node, createEventType, createTimerAction);
    // Action timerAction = readSingleAction(processDefinition, element);
    // if (timerAction != null) {
    // timerAction.setName(name);
    // addAction(node, Event.EVENTTYPE_TIMER, timerAction);
    // }
    //
    // CancelTimerAction cancelTimerAction =
    // ApplicationContextFactory.createAutowiredBean(CancelTimerAction.class);
    // cancelTimerAction.setName(name);
    // String cancelEventType = node instanceof TaskDefinition ?
    // Event.EVENTTYPE_TASK_END : Event.EVENTTYPE_NODE_LEAVE;
    // addAction(node, cancelEventType, cancelTimerAction);
    // }
    // }
    //
    // private void readEvents(ProcessDefinition processDefinition, Element
    // parentElement, GraphElement graphElement) {
    // List<Element> elements = parentElement.elements(EVENT_NODE);
    // for (Element eventElement : elements) {
    // String eventType = eventElement.attributeValue(TYPE_ATTR);
    // readActions(processDefinition, eventElement, graphElement, eventType);
    // }
    // }

    // private void readActions(ProcessDefinition processDefinition, Element
    // eventElement, GraphElement graphElement, String eventType) {
    // // for all the elements in the event element
    // List<Element> elements = eventElement.elements();
    // for (Element actionElement : elements) {
    // Action action = createAction(processDefinition, actionElement);
    // addAction(graphElement, eventType, action);
    // }
    // }

    // private void addAction(GraphElement graphElement, String eventType,
    // Action action) {
    // Event event = graphElement.getEvent(eventType);
    // if (event == null) {
    // event = new Event(eventType);
    // graphElement.addEvent(event);
    // }
    // action.setParent(graphElement);
    // event.addAction(action);
    // }

    //
    // private Action readSingleAction(ProcessDefinition processDefinition,
    // Element nodeElement) {
    // Action action = null;
    // // search for the first action element in the node
    // Iterator iter = nodeElement.elementIterator();
    // while (iter.hasNext() && (action == null)) {
    // Element candidate = (Element) iter.next();
    // // parse the action and assign it to this node
    // action = createAction(processDefinition, candidate);
    // }
    // return action;
    // }
    //
    // private Action createAction(ProcessDefinition processDefinition, Element
    // element) {
    // String actionName = element.getName();
    // try {
    // Action action = new Action();
    // action.setName(element.attributeValue(NAME_ATTR));
    // action.setDelegation(readDelegation(processDefinition, element));
    // String acceptPropagatedEvents =
    // element.attributeValue("accept-propagated-events");
    // if ("false".equalsIgnoreCase(acceptPropagatedEvents)) {
    // action.setPropagationAllowed(false);
    // }
    // return action;
    // } catch (Exception e) {
    // throw new InvalidDefinitionException("couldn't create action '" +
    // actionName + "'", e);
    // }
    // }

    private Delegation readDelegation(ProcessDefinition processDefinition, Element element) {
        String className = element.attributeValue(CLASS_ATTR);
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
                    // when parsing, it could be to store the config in the
                    // database, so we want to make the configuration compact
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

    private void verifyElements(ProcessDefinition processDefinition) {
        for (SwimlaneDefinition swimlaneDefinition : processDefinition.getSwimlanes().values()) {
            SwimlaneDefinition startTaskSwimlane = processDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
            if (swimlaneDefinition.getDelegation() == null && swimlaneDefinition != startTaskSwimlane) {
                throw new InvalidDefinitionException("swimlane '" + swimlaneDefinition + "' does not have an assignment");
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

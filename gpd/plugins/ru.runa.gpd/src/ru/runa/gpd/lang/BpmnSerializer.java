package ru.runa.gpd.lang;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.eclipse.core.resources.IFile;

import ru.runa.gpd.Localization;
import ru.runa.gpd.Version;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Describable;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.ExclusiveGateway;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ParallelGateway;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ReceiveMessageNode;
import ru.runa.gpd.lang.model.ScriptTask;
import ru.runa.gpd.lang.model.SendMessageNode;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.State;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.TextAnnotation;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.ui.dialog.ErrorDialog;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.lang.AsyncCompletionMode;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class BpmnSerializer extends ProcessSerializer {
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
    private static final String TOKEN = "token";
    private static final String TEXT_ANNOTATION = "textAnnotation";
    private static final String TEXT = "text";
    private static final String SERVICE_TASK = "serviceTask";
    private static final String SCRIPT_TASK = "scriptTask";
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
    private static final String MULTI_INSTANCE = "multiInstance";
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
    private static final String TIMER_GLOBAL = "__GLOBAL";//
    private static final String TIMER_ESCALATION = "__ESCALATION";//
    private static final String BOUNDARY_EVENT = "boundaryEvent";
    private static final String INTERMEDIATE_EVENT = "intermediateCatchEvent";
    private static final String CANCEL_ACTIVITY = "cancelActivity";
    private static final String ATTACHED_TO_REF = "attachedToRef";
    private static final String TIMER_EVENT = "timerEventDefinition";
    private static final String TIMER_DURATION = "timeDuration";
    private static final String VERSION = "version";
    private static final String ASYNC = "async";
    private static final String ASYNC_COMPLETION_MODE = "asyncCompletionMode";

    @Override
    public boolean isSupported(Document document) {
        return DEFINITIONS.equals(document.getRootElement().getName());
    }

    @Override
    public Document getInitialProcessDefinitionDocument(String processName, Map<String, String> properties) {
        Document document = XmlUtil.createDocument(DEFINITIONS);
        Element definitionsElement = document.getRootElement();
        definitionsElement.addNamespace(BPMN_PREFIX, BPMN_NAMESPACE);
        definitionsElement.addNamespace("bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI");
        definitionsElement.addNamespace("omgdc", "http://www.omg.org/spec/DD/20100524/DC");
        definitionsElement.addNamespace("omgdi", "http://www.omg.org/spec/DD/20100524/DI");
        definitionsElement.addNamespace(RUNA_PREFIX, RUNA_NAMESPACE);
        definitionsElement.addAttribute("targetNamespace", RUNA_NAMESPACE);
        Element process = definitionsElement.addElement(PROCESS, BPMN_NAMESPACE);
        process.addAttribute(NAME, processName);
        if (properties != null) {
            writeExtensionElements(process, properties);
        }
        return document;
    }

    @Override
    public void saveToXML(ProcessDefinition definition, Document document) {
        Element definitionsElement = document.getRootElement();
        Element process = definitionsElement.element(QName.get(PROCESS, BPMN_NAMESPACE));
        process.addAttribute(NAME, definition.getName());
        Map<String, String> processProperties = Maps.newHashMap();
        if (definition.getDefaultTaskTimeoutDelay().hasDuration()) {
            processProperties.put(DEFAULT_TASK_TIMOUT, definition.getDefaultTaskTimeoutDelay().getDuration());
        }
        if (definition.getSwimlaneDisplayMode() != null) {
            processProperties.put(SWIMLANE_DISPLAY_MODE, definition.getSwimlaneDisplayMode().name());
        }
        processProperties.put(VERSION, Version.get());
        writeExtensionElements(process, processProperties);
        if (definition.isInvalid()) {
            process.addAttribute(EXECUTABLE, "false");
        }
        if (definition.getDescription() != null && definition.getDescription().length() > 0) {
            Element desc = process.addElement(DOCUMENTATION);
            setNodeValue(desc, definition.getDescription());
        }
        Element laneSetElement = process.addElement(SWIMLANE_SET).addAttribute(ID, "laneSet1");
        List<Swimlane> swimlanes = definition.getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            Element swimlaneElement = writeElement(laneSetElement, swimlane);
            writeDelegation(swimlaneElement, swimlane);
            List<GraphElement> swimlaneElements = definition.getContainerElements(swimlane);
            for (GraphElement child : swimlaneElements) {
                swimlaneElement.addElement(FLOW_NODE_REF).addText(child.getId());
            }
        }
        StartState startState = definition.getFirstChild(StartState.class);
        if (startState != null) {
            writeTaskState(process, startState);
            writeTransitions(process, startState);
        }
        List<ExclusiveGateway> exclusiveGateways = definition.getChildren(ExclusiveGateway.class);
        for (ExclusiveGateway gateway : exclusiveGateways) {
            writeNode(process, gateway);
        }
        List<TaskState> taskStates = definition.getChildren(TaskState.class);
        for (TaskState taskState : taskStates) {
            writeTaskState(process, taskState);
            writeTransitions(process, taskState);
            Timer timer = taskState.getTimer();
            if (timer != null) {
                Element boundaryEventElement = process.addElement(BOUNDARY_EVENT);
                writeTimer(boundaryEventElement, timer);
                boundaryEventElement.addAttribute(CANCEL_ACTIVITY, "true");
                boundaryEventElement.addAttribute(ATTACHED_TO_REF, taskState.getId());
                writeTransitions(process, timer);
            }
            // if (state.isUseEscalation()) {
            // String timerName = TIMER_ESCALATION;
            // Delay escalationDuration = state.getEscalationTime();
            // Element timerElement = stateElement.addElement(TIMER_NODE);
            // setAttribute(timerElement, NAME_ATTR, timerName);
            // if (escalationDuration != null &&
            // escalationDuration.hasDuration()) {
            // setAttribute(timerElement, DUEDATE_ATTR,
            // escalationDuration.getDuration());
            // }
            // TimerAction escalationAction = state.getEscalationAction();
            // if (escalationAction != null) {
            // if (escalationAction.getRepeat().hasDuration()) {
            // setAttribute(timerElement, REPEAT_ATTR,
            // escalationAction.getRepeat().getDuration());
            // }
            // writeDelegation(timerElement, ACTION_NODE, escalationAction);
            // }
            // }
        }
        List<Timer> timers = definition.getChildren(Timer.class);
        for (Timer timer : timers) {
            Element intermediateEventElement = process.addElement(INTERMEDIATE_EVENT);
            writeTimer(intermediateEventElement, timer);
            writeTransitions(process, timer);
        }
        List<ScriptTask> scriptTasks = definition.getChildren(ScriptTask.class);
        for (ScriptTask scriptTask : scriptTasks) {
            writeNode(process, scriptTask);
        }
        List<ParallelGateway> parallelGateways = definition.getChildren(ParallelGateway.class);
        for (ParallelGateway gateway : parallelGateways) {
            writeNode(process, gateway);
        }
        List<Subprocess> subprocesses = definition.getChildren(Subprocess.class);
        for (Subprocess subprocess : subprocesses) {
            Element element = writeNode(process, subprocess);
            element.addAttribute(RUNA_PREFIX + ":" + PROCESS, subprocess.getSubProcessName());
            if (subprocess instanceof MultiSubprocess) {
                Map<String, String> properties = Maps.newHashMap();
                properties.put(MULTI_INSTANCE, "true");
                writeExtensionElements(element, properties);
            }
            writeVariables(element, subprocess.getVariableMappings());
        }
        List<SendMessageNode> sendMessageNodes = definition.getChildren(SendMessageNode.class);
        for (SendMessageNode messageNode : sendMessageNodes) {
            Element messageElement = writeNode(process, messageNode);
            messageElement.addAttribute(RUNA_PREFIX + ":" + TIMER_DURATION, messageNode.getTtlDuration().getDuration());
            writeVariables(messageElement, messageNode.getVariablesList());
        }
        List<ReceiveMessageNode> receiveMessageNodes = definition.getChildren(ReceiveMessageNode.class);
        for (ReceiveMessageNode messageNode : receiveMessageNodes) {
            Element messageElement = writeNode(process, messageNode);
            writeVariables(messageElement, messageNode.getVariablesList());
            // TODO duplicated
            Timer timer = messageNode.getTimer();
            if (timer != null) {
                Element boundaryEventElement = process.addElement(BOUNDARY_EVENT);
                writeTimer(boundaryEventElement, timer);
                boundaryEventElement.addAttribute(CANCEL_ACTIVITY, "true");
                boundaryEventElement.addAttribute(ATTACHED_TO_REF, messageNode.getId());
                writeTransitions(process, timer);
            }
        }
        List<EndTokenState> endTokenStates = definition.getChildren(EndTokenState.class);
        for (EndTokenState endTokenState : endTokenStates) {
            Element element = writeElement(process, endTokenState);
            Map<String, String> properties = Maps.newHashMap();
            properties.put(TOKEN, "true");
            writeExtensionElements(element, properties);
        }
        List<EndState> endStates = definition.getChildren(EndState.class);
        for (EndState endState : endStates) {
            writeElement(process, endState);
        }
        List<TextAnnotation> textAnnotations = definition.getChildren(TextAnnotation.class);
        for (TextAnnotation textAnnotation : textAnnotations) {
            Element element = process.addElement(textAnnotation.getTypeDefinition().getBpmnElementName());
            setAttribute(element, ID, textAnnotation.getId());
            String description = textAnnotation.getDescription();
            if (!Strings.isNullOrEmpty(description)) {
                element.addElement(TEXT).addCDATA(description);
            }
        }
        // TODO instead of gpd.xml
        // Element diagramElement =
        // definitionsElement.addElement("bpmndi:BPMNDiagram");
        // diagramElement.addAttribute(ID_ATTR, "test");
    }

    private void writeVariables(Element element, List<VariableMapping> variableMappings) {
        // TODO Element ioSpecificationElement =
        // element.addElement(IO_SPECIFICATION);
        Map<String, Object> properties = Maps.newHashMap();
        properties.put(VARIABLES, variableMappings);
        writeExtensionElements(element, properties);
    }

    private Element writeNode(Element parent, Node node) {
        Element nodeElement = writeElement(parent, node);
        if (node.isDelegable()) {
            writeDelegation(nodeElement, (Delegable) node);
        }
        writeTransitions(parent, node);
        return nodeElement;
    }

    private Element writeTaskState(Element parent, SwimlanedNode swimlanedNode) {
        Element nodeElement = writeElement(parent, swimlanedNode);
        Map<String, String> properties = Maps.newHashMap();
        properties.put(SWIMLANE, swimlanedNode.getSwimlaneName());
        if (swimlanedNode instanceof TaskState) {
            TaskState taskState = (TaskState) swimlanedNode;
            if (taskState.isAsync()) {
                properties.put(ASYNC, Boolean.TRUE.toString());
                properties.put(ASYNC_COMPLETION_MODE, taskState.getAsyncCompletionMode().name());
            }
        }
        if (swimlanedNode instanceof State && ((State) swimlanedNode).isReassignmentEnabled()) {
            properties.put(REASSIGN, "true");
        }
        writeExtensionElements(nodeElement, properties);
        // for (Action action : state.getActions()) {
        // ActionImpl actionImpl = (ActionImpl) action;
        // writeEvent(taskElement, new Event(actionImpl.getEventType()),
        // actionImpl);
        // }
        return nodeElement;
    }

    private void writeTimer(Element parent, Timer timer) {
        if (timer == null) {
            return;
        }
        setAttribute(parent, ID, timer.getId());
        setAttribute(parent, NAME, timer.getName());
        Element eventElement = parent.addElement(timer.getTypeDefinition().getBpmnElementName());
        if (!Strings.isNullOrEmpty(timer.getDescription())) {
            eventElement.addElement(DOCUMENTATION).addCDATA(timer.getDescription());
        }
        Element durationElement = eventElement.addElement(TIMER_DURATION);
        durationElement.addText(timer.getDelay().getDuration());
        if (timer.getAction() != null) {
            // Map<String, String> properties
            // if (timer.getAction().getRepeatDelay().hasDuration()) {
            // setAttribute(timerElement, REPEAT_ATTR,
            // timer.getAction().getRepeatDelay().getDuration());
            // }
            // writeDelegation(timerElement, ACTION_NODE, timer.getAction());
            // TODO
        }
    }

    private Element writeElement(Element parent, GraphElement element) {
        String bpmnElementName;
        if (element instanceof EndTokenState) {
            bpmnElementName = END_STATE;
        } else if (element instanceof MultiSubprocess) {
            bpmnElementName = SUBPROCESS;
        } else {
            bpmnElementName = element.getTypeDefinition().getBpmnElementName();
        }
        Element result = parent.addElement(bpmnElementName);
        setAttribute(result, ID, element.getId());
        if (element instanceof NamedGraphElement) {
            setAttribute(result, NAME, ((NamedGraphElement) element).getName());
        }
        if (element instanceof Describable) {
            String description = ((Describable) element).getDescription();
            if (!Strings.isNullOrEmpty(description)) {
                result.addElement(DOCUMENTATION).addCDATA(description);
            }
        }
        return result;
    }

    private void writeTransitions(Element parent, Node node) {
        List<Transition> transitions = node.getLeavingTransitions();
        for (Transition transition : transitions) {
            Element transitionElement = parent.addElement(SEQUENCE_FLOW);
            transitionElement.addAttribute(ID, transition.getId());
            transitionElement.addAttribute(NAME, transition.getName());
            String sourceNodeId = transition.getSource().getId();
            String targetNodeId = transition.getTarget().getId();
            if (Objects.equal(sourceNodeId, targetNodeId)) {
                throw new IllegalArgumentException("Invalid transition " + transition);
            }
            transitionElement.addAttribute(SOURCE_REF, sourceNodeId);
            transitionElement.addAttribute(TARGET_REF, targetNodeId);
            // for (Action action : transition.getActions()) {
            // writeDelegation(transitionElement, ACTION_NODE, action);
            // }
        }
    }

    private Element writeExtensionElements(Element parent, Map<String, ? extends Object> properties) {
        if (properties.isEmpty()) {
            return null;
        }
        Element extensionsElement = parent.element(EXTENSION_ELEMENTS);
        if (extensionsElement == null) {
            extensionsElement = parent.addElement(EXTENSION_ELEMENTS);
        }
        List<VariableMapping> variableMappings = (List<VariableMapping>) properties.remove(VARIABLES);
        if (variableMappings != null) {
            Element variablesElement = extensionsElement.addElement(RUNA_PREFIX + ":" + VARIABLES);
            for (VariableMapping variableMapping : variableMappings) {
                Element variableElement = variablesElement.addElement(RUNA_PREFIX + ":" + VARIABLE);
                setAttribute(variableElement, NAME, variableMapping.getProcessVariable());
                setAttribute(variableElement, MAPPED_NAME, variableMapping.getSubprocessVariable());
                setAttribute(variableElement, USAGE, variableMapping.getUsage());
            }
        }
        for (Map.Entry<String, ? extends Object> entry : properties.entrySet()) {
            if (entry.getValue() != null) {
                Element propertyElement = extensionsElement.addElement(RUNA_PREFIX + ":" + PROPERTY);
                propertyElement.addAttribute(NAME, entry.getKey());
                propertyElement.addAttribute(VALUE, entry.getValue().toString());
            }
        }
        return extensionsElement;
    }

    private void writeDelegation(Element parent, Delegable delegable) {
        Map<String, Object> properties = Maps.newHashMap();
        properties.put(CLASS, delegable.getDelegationClassName());
        Element extensionsElement = writeExtensionElements(parent, properties);
        extensionsElement.addElement(RUNA_PREFIX + ":" + PROPERTY).addAttribute(NAME, CONFIG).addCDATA(delegable.getDelegationConfiguration());
    }

    @Override
    public void validateProcessDefinitionXML(IFile file) {
        try {
            XmlUtil.parseWithXSDValidation(file.getContents(), "BPMN20.xsd");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends GraphElement> T create(Element node, GraphElement parent) {
        Map<String, String> properties = parseExtensionProperties(node);
        String bpmnElementName;
        if (properties.containsKey(TOKEN)) {
            bpmnElementName = "endTokenEvent";
        } else if (properties.containsKey(MULTI_INSTANCE)) {
            bpmnElementName = "multiProcess";
        } else {
            bpmnElementName = node.getName();
        }
        GraphElement element = NodeRegistry.getNodeTypeDefinition(Language.BPMN, bpmnElementName).createElement(parent);
        String nodeId = node.attributeValue(ID);
        String name = node.attributeValue(NAME);
        if (element instanceof Node && nodeId == null) {
            nodeId = name;
        }
        element.setId(nodeId);
        if (parent != null) {
            parent.addChild(element);
        }
        if (element instanceof NamedGraphElement) {
            ((NamedGraphElement) element).setName(name);
        }
        List<Element> nodeList = node.elements();
        for (Element childNode : nodeList) {
            if (DOCUMENTATION.equals(childNode.getName())) {
                ((Describable) element).setDescription(childNode.getTextTrim());
            }
            if (TIMER_DURATION.equals(childNode.getName())) {
                ((Timer) element).setDelay(new Duration(childNode.getTextTrim()));
                // List<Element> actionNodes = childNode.elements();
                // for (Element aa : actionNodes) {
                // if (ACTION_NODE.equals(aa.getName())) {
                // TimerAction timerAction = new TimerAction(null);
                // timerAction.setDelegationClassName(aa.attributeValue(CLASS_ATTR));
                // timerAction.setDelegationConfiguration(aa.getTextTrim());
                // timerAction.setRepeat(childNode.attributeValue(REPEAT_ATTR));
                // messageNode.setTimerAction(timerAction);
                // }
                // }
            }
            // if (ACTION_NODE.equals(childNode.getName())) {
            // // only transition actions loaded here
            // String eventType;
            // if (element instanceof Transition) {
            // eventType = Event.TRANSITION;
            // } else {
            // throw new
            // RuntimeException("Unexpected action in XML, context of " +
            // element);
            // }
            // parseAction(childNode, element, eventType);
            // }
        }
        if (element instanceof Delegable) {
            element.setDelegationClassName(properties.get(CLASS));
            element.setDelegationConfiguration(properties.get(CONFIG));
        }
        return (T) element;
    }

    // private void parseAction(Element node, GraphElement parent, String
    // eventType) {
    // ActionImpl action =
    // NodeRegistry.getNodeTypeDefinition(ActionImpl.class).createElement(parent);
    // action.setDelegationClassName(node.attributeValue(CLASS_ATTR));
    // action.setDelegationConfiguration(node.getTextTrim());
    // parent.addAction(action, -1);
    // action.setEventType(eventType);
    // }
    private Map<String, String> parseExtensionProperties(Element element) {
        Map<String, String> map = Maps.newHashMap();
        Element extensionsElement = element.element(EXTENSION_ELEMENTS);
        if (extensionsElement != null) {
            List<Element> propertyElements = extensionsElement.elements(QName.get(PROPERTY, RUNA_NAMESPACE));
            for (Element propertyElement : propertyElements) {
                String name = propertyElement.attributeValue(NAME);
                String value = propertyElement.attributeValue(VALUE);
                if (value == null) {
                    value = propertyElement.getText();
                }
                map.put(name, value);
            }
        }
        return map;
    }

    private List<VariableMapping> parseVariableMappings(Element element) {
        List<VariableMapping> list = Lists.newArrayList();
        Element extensionsElement = element.element(EXTENSION_ELEMENTS);
        if (extensionsElement != null) {
            Element variablesElement = extensionsElement.element(QName.get(VARIABLES, RUNA_NAMESPACE));
            if (variablesElement != null) {
                List<Element> variableElements = variablesElement.elements(QName.get(VARIABLE, RUNA_NAMESPACE));
                for (Element variableElement : variableElements) {
                    VariableMapping variableMapping = new VariableMapping();
                    variableMapping.setProcessVariable(variableElement.attributeValue(NAME));
                    variableMapping.setSubprocessVariable(variableElement.attributeValue(MAPPED_NAME));
                    variableMapping.setUsage(variableElement.attributeValue(USAGE));
                    list.add(variableMapping);
                }
            }
        }
        return list;
    }

    @Override
    public ProcessDefinition parseXML(Document document) {
        Element definitionsElement = document.getRootElement();
        Element process = definitionsElement.element(PROCESS);
        ProcessDefinition definition = create(process, null);
        Map<String, String> processProperties = parseExtensionProperties(process);
        String defaultTaskTimeout = processProperties.get(DEFAULT_TASK_TIMOUT);
        if (!Strings.isNullOrEmpty(defaultTaskTimeout)) {
            definition.setDefaultTaskTimeoutDelay(new Duration(defaultTaskTimeout));
        }
        String swimlaneDisplayModeName = processProperties.get(SWIMLANE_DISPLAY_MODE);
        if (swimlaneDisplayModeName != null) {
            definition.setSwimlaneDisplayMode(SwimlaneDisplayMode.valueOf(swimlaneDisplayModeName));
        }
        Map<Swimlane, List<String>> swimlaneElementIds = Maps.newHashMap();
        Element swimlaneSetElement = process.element(SWIMLANE_SET);
        if (swimlaneSetElement != null) {
            List<Element> swimlanes = swimlaneSetElement.elements(SWIMLANE);
            for (Element swimlaneElement : swimlanes) {
                Swimlane swimlane = create(swimlaneElement, definition);
                // Map<String, String> swimlaneProperties =
                // parseExtensionProperties(swimlaneElement);
                // swimlane.setDelegationClassName(swimlaneProperties.get(CLASS));
                // swimlane.setDelegationConfiguration(swimlaneProperties.get(CONFIG));
                List<Element> flowNodeRefElements = swimlaneElement.elements(FLOW_NODE_REF);
                List<String> flowNodeIds = Lists.newArrayList();
                for (Element flowNodeRefElement : flowNodeRefElements) {
                    flowNodeIds.add(flowNodeRefElement.getTextTrim());
                }
                swimlaneElementIds.put(swimlane, flowNodeIds);
            }
        }
        List<Element> startStates = process.elements(START_EVENT);
        if (startStates.size() > 0) {
            if (startStates.size() > 1) {
                ErrorDialog.open(Localization.getString("model.validation.multipleStartStatesNotAllowed"));
            }
            Element startStateElement = startStates.get(0);
            StartState startState = create(startStateElement, definition);
            String swimlaneName = parseExtensionProperties(startStateElement).get(SWIMLANE);
            Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
            startState.setSwimlane(swimlane);
        }
        List<Element> taskStateElements = process.elements(USER_TASK);
        for (Element taskStateElement : taskStateElements) {
            TaskState state = create(taskStateElement, definition);
            if (state instanceof TaskState) {
                Map<String, String> properties = parseExtensionProperties(taskStateElement);
                String swimlaneName = properties.get(SWIMLANE);
                Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                state.setSwimlane(swimlane);
                String reassign = properties.get(REASSIGN);
                if (reassign != null) {
                    boolean forceReassign = Boolean.parseBoolean(reassign);
                    state.setReassignmentEnabled(forceReassign);
                }
                String async = properties.get(ASYNC);
                if (async != null) {
                    state.setAsync(Boolean.parseBoolean(async));
                }
                String asyncCompletionMode = properties.get(ASYNC_COMPLETION_MODE);
                if (asyncCompletionMode != null) {
                    state.setAsyncCompletionMode(AsyncCompletionMode.valueOf(asyncCompletionMode));
                }
            }
            // String duedateAttr = stateNodeChild.attributeValue(DUEDATE_ATTR);
            // if (duedateAttr != null) {
            // state.setTimeOutDueDate(duedateAttr);
            // }
            // List<Element> aaa = stateNodeChild.elements();
            // for (Element a : aaa) {
            // if (EVENT_NODE.equals(a.getName())) {
            // String eventType = a.attributeValue(TYPE_ATTR);
            // List<Element> actionNodes = a.elements();
            // for (Element aa : actionNodes) {
            // if (ACTION_NODE.equals(aa.getName())) {
            // parseAction(aa, state, eventType);
            // }
            // }
            // }
            // }
            // if (TIMER_NODE.equals(stateNodeChild.getName())) {
            // String nameTimer = stateNodeChild.attributeValue(NAME_ATTR);
            // String dueDate = stateNodeChild.attributeValue(DUEDATE_ATTR);
            // if (TIMER_ESCALATION.equals(nameTimer)) {
            // ((TaskState) state).setUseEscalation(true);
            // if (dueDate != null) {
            // ((TaskState) state).setEscalationTime(new Delay(dueDate));
            // }
            // } else if (TIMER_GLOBAL_NAME.equals(nameTimer)) {
            // definition.setTimeOutDueDate(dueDate);
            // } else {
            // state.setHasTimer(true);
            // if (dueDate != null) {
            // state.setDueDate(dueDate);
            // }
            // }
            // List<Element> actionNodes = stateNodeChild.elements();
            // for (Element aa : actionNodes) {
            // if (ACTION_NODE.equals(aa.getName())) {
            // TimerAction timerAction = new TimerAction(null);
            // timerAction.setDelegationClassName(aa.attributeValue(CLASS_ATTR));
            // timerAction.setDelegationConfiguration(aa.getTextTrim());
            // timerAction.setRepeat(stateNodeChild.attributeValue(REPEAT_ATTR));
            // if (TIMER_GLOBAL_NAME.equals(nameTimer)) {
            // definition.setTimeOutAction(timerAction);
            // } else if (TIMER_ESCALATION.equals(nameTimer)) {
            // ((TaskState) state).setEscalationAction(timerAction);
            // } else {
            // ((ITimed) state).setTimerAction(timerAction);
            // }
            // }
            // }
            // }
        }
        {
            // backward compatibility: versions affected: 4.0.0 .. 4.0.4
            List<Element> scriptTaskElements = process.elements(SERVICE_TASK);
            for (Element node : scriptTaskElements) {
                create(node, definition);
            }
        }
        List<Element> scriptTaskElements = process.elements(SCRIPT_TASK);
        for (Element node : scriptTaskElements) {
            create(node, definition);
        }
        List<Element> parallelGatewayElements = process.elements(PARALLEL_GATEWAY);
        for (Element node : parallelGatewayElements) {
            create(node, definition);
        }
        List<Element> exclusiveGatewayElements = process.elements(EXCLUSIVE_GATEWAY);
        for (Element node : exclusiveGatewayElements) {
            create(node, definition);
        }
        List<Element> subprocessElements = process.elements(SUBPROCESS);
        for (Element subprocessElement : subprocessElements) {
            Subprocess subprocess = create(subprocessElement, definition);
            subprocess.setSubProcessName(subprocessElement.attributeValue(QName.get(PROCESS, RUNA_NAMESPACE)));
            subprocess.setVariableMappings(parseVariableMappings(subprocessElement));
        }
        List<Element> sendMessageElements = process.elements(SEND_MESSAGE);
        for (Element messageElement : sendMessageElements) {
            SendMessageNode messageNode = create(messageElement, definition);
            String duration = messageElement.attributeValue(RUNA_PREFIX + ":" + TIMER_DURATION, "1 days");
            messageNode.setTtlDuration(new Duration(duration));
            messageNode.setVariablesList(parseVariableMappings(messageElement));
        }
        List<Element> receiveMessageElements = process.elements(RECEIVE_MESSAGE);
        for (Element messageElement : receiveMessageElements) {
            ReceiveMessageNode messageNode = create(messageElement, definition);
            messageNode.setVariablesList(parseVariableMappings(messageElement));
        }
        List<Element> intermediateEventElements = process.elements(INTERMEDIATE_EVENT);
        for (Element intermediateEventElement : intermediateEventElements) {
            List<Element> eventElements = intermediateEventElement.elements();
            for (Element eventElement : eventElements) {
                Timer timer = create(eventElement, definition);
                timer.setId(intermediateEventElement.attributeValue(ID));
                timer.setName(intermediateEventElement.attributeValue(NAME));
            }
        }
        List<Element> boundaryEventElements = process.elements(BOUNDARY_EVENT);
        for (Element boundaryEventElement : boundaryEventElements) {
            List<Element> eventElements = boundaryEventElement.elements();
            String parentNodeId = boundaryEventElement.attributeValue(ATTACHED_TO_REF);
            GraphElement parent = definition.getGraphElementByIdNotNull(parentNodeId);
            for (Element eventElement : eventElements) {
                Timer timer = create(eventElement, parent);
                timer.setId(boundaryEventElement.attributeValue(ID));
                timer.setName(boundaryEventElement.attributeValue(NAME));
                timer.setParentContainer(parent);
            }
        }
        List<Element> endStates = process.elements(END_STATE);
        for (Element node : endStates) {
            create(node, definition);
        }
        List<Element> textAnnotationElements = process.elements(TEXT_ANNOTATION);
        for (Element textAnnotationElement : textAnnotationElements) {
            TextAnnotation textAnnotation = create(textAnnotationElement, definition);
            textAnnotation.setDescription(textAnnotationElement.elementTextTrim(TEXT));
        }
        List<Element> transitions = process.elements(SEQUENCE_FLOW);
        for (Element transitionElement : transitions) {
            Node source = definition.getGraphElementByIdNotNull(transitionElement.attributeValue(SOURCE_REF));
            Node target = definition.getGraphElementByIdNotNull(transitionElement.attributeValue(TARGET_REF));
            Transition transition = NodeRegistry.getNodeTypeDefinition(Transition.class).createElement(source);
            transition.setId(transitionElement.attributeValue(ID));
            transition.setName(transitionElement.attributeValue(NAME));
            transition.setTarget(target);
            source.addLeavingTransition(transition);
        }
        for (Map.Entry<Swimlane, List<String>> entry : swimlaneElementIds.entrySet()) {
            for (String nodeId : entry.getValue()) {
                definition.getGraphElementByIdNotNull(nodeId).setParentContainer(entry.getKey());
            }
        }
        return definition;
    }
}

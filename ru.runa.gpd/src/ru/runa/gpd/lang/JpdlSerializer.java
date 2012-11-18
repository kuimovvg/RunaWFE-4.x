package ru.runa.gpd.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.ActionImpl;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Describable;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Event;
import ru.runa.gpd.lang.model.Fork;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Join;
import ru.runa.gpd.lang.model.MultiInstance;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ReceiveMessageNode;
import ru.runa.gpd.lang.model.SendMessageNode;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.State;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.WaitState;
import ru.runa.gpd.ui.dialog.ErrorDialog;
import ru.runa.gpd.util.TimerDuration;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.XmlUtil;

@SuppressWarnings("unchecked")
public class JpdlSerializer extends ProcessSerializer {
    private static final String ROOT_ELEMENT = "process-definition";
    private static final String INVALID_ATTR = "invalid";
    private static final String ACCESS_ATTR = "access";
    private static final String END_STATE_NODE = "end-state";
    private static final String VARIABLE_NODE = "variable";
    private static final String SUB_PROCESS_NODE = "sub-process";
    private static final String MAPPED_NAME_ATTR = "mapped-name";
    private static final String PROCESS_STATE_NODE = "process-state";
    private static final String MULTI_INSTANCE_STATE_NODE = "multiinstance-state";
    private static final String DECISION_NODE = "decision";
    private static final String JOIN_NODE = "join";
    private static final String FORK_NODE = "fork";
    private static final String DUEDATE_ATTR = "duedate";
    private static final String DEFAULT_DUEDATE_ATTR = "default-task-duedate";
    private static final String REPEAT_ATTR = "repeat";
    private static final String TIMER_NODE = "timer";
    private static final String ASSIGNMENT_NODE = "assignment";
    private static final String TASK_STATE_NODE = "task-node";
    private static final String TASK_NODE = "task";
    private static final String WAIT_STATE_NODE = "wait-state";
    private static final String START_STATE_NODE = "start-state";
    private static final String SWIMLANE_NODE = "swimlane";
    private static final String REASSIGN_ATTR = "reassign";
    private static final String TO_ATTR = "to";
    private static final String CLASS_ATTR = "class";
    private static final String ACTION_NODE = "action";
    private static final String EVENT_NODE = "event";
    private static final String TRANSITION_NODE = "transition";
    private static final String HANDLER_NODE = "handler";
    private static final String DESCRIPTION_NODE = "description";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";
    private static final String ID_ATTR = "id";
    private static final String SWIMLANE_ATTR = "swimlane";
    private static final String TRANSITION_ATTR = "transition";
    private static final String SEND_MESSAGE_NODE = "send-message";
    private static final String RECEIVE_MESSAGE_NODE = "receive-message";
    private static final String TIMER_GLOBAL_NAME = "__GLOBAL";
    private static final String TIMER_ESCALATION = "__ESCALATION";

    @Override
    public boolean isSupported(Document document) {
        return ROOT_ELEMENT.equals(document.getRootElement().getName());
    }

    @Override
    public Document getInitialProcessDefinitionDocument(String processName) {
        Document document = XmlUtil.createDocument(ROOT_ELEMENT);
        document.getRootElement().addAttribute(NAME_ATTR, processName);
        return document;
    }

    @Override
    public void validateProcessDefinitionXML(IFile file) {
        //        try {
        //            XmlUtil.parseWithXSDValidation(getClass().getResourceAsStream("/schema/" + XSD_FILE_NAME));
        //        } catch (Exception e) {
        //            throw new RuntimeException(e);
        //        } TODO
    }

    private <T extends GraphElement> T create(Element node, GraphElement parent) {
        return create(node, parent, node.getName());
    }

    private <T extends GraphElement> T create(Element node, GraphElement parent, String typeName) {
        GraphElement element = NodeRegistry.getNodeTypeDefinition(typeName).createElement(parent);
        if (parent != null) {
            parent.addChild(element);
        }
        if (element instanceof Node) {
            String nodeId = node.attributeValue(ID_ATTR);
            if (nodeId == null) {
                nodeId = ((Node) element).getName();
            }
            ((Node) element).setNodeId(nodeId);
        }
        if (element instanceof NamedGraphElement) {
            ((NamedGraphElement) element).setName(node.attributeValue(NAME_ATTR));
        }
        List<Element> nodeList = node.elements();
        for (Element childNode : nodeList) {
            if (DESCRIPTION_NODE.equals(childNode.getName())) {
                ((Describable) element).setDescription(childNode.getTextTrim());
            }
            if (HANDLER_NODE.equals(childNode.getName()) || ASSIGNMENT_NODE.equals(childNode.getName())) {
                ((Delegable) element).setDelegationClassName(childNode.attributeValue(CLASS_ATTR));
                element.setDelegationConfiguration(childNode.getTextTrim());
            }
            if (ACTION_NODE.equals(childNode.getName())) {
                // only transition actions loaded here
                String eventType;
                if (element instanceof Transition) {
                    eventType = Event.TRANSITION;
                } else {
                    throw new RuntimeException("Unexpected action in XML, context of " + element);
                }
                parseAction(childNode, element, eventType);
            }
            if (TRANSITION_NODE.equals(childNode.getName())) {
                parseTransition(childNode, element);
            }
        }
        return (T) element;
    }

    private void parseTransition(Element node, GraphElement parent) {
        Transition transition = create(node, parent);
        String targetName = node.attributeValue(TO_ATTR);
        TRANSITION_TARGETS.put(transition, targetName);
    }

    private void parseAction(Element node, GraphElement parent, String eventType) {
        ActionImpl action = NodeRegistry.getNodeTypeDefinition(ACTION_NODE).createElement(parent);
        action.setDelegationClassName(node.attributeValue(CLASS_ATTR));
        action.setDelegationConfiguration(node.getTextTrim());
        parent.addAction(action, -1);
        action.setEventType(eventType);
    }

    private static Map<Transition, String> TRANSITION_TARGETS = new HashMap<Transition, String>();

    @Override
    public ProcessDefinition parseXML(Document document) {
        TRANSITION_TARGETS.clear();
        Element root = document.getRootElement();
        ProcessDefinition definition = create(root, null);
        definition.setDefaultTaskDuedate(root.attributeValue(DEFAULT_DUEDATE_ATTR));
        List<Element> swimlanes = root.elements(SWIMLANE_NODE);
        for (Element node : swimlanes) {
            create(node, definition);
        }
        List<Element> startStates = root.elements(START_STATE_NODE);
        if (startStates.size() > 0) {
            if (startStates.size() > 1) {
                ErrorDialog.open(Localization.getString("model.validation.multipleStartStatesNotAllowed"));
            }
            Element node = startStates.get(0);
            StartState startState = create(node, definition);
            List<Element> stateChilds = node.elements();
            for (Element stateNodeChild : stateChilds) {
                if (TASK_NODE.equals(stateNodeChild.getName())) {
                    String swimlaneName = stateNodeChild.attributeValue(SWIMLANE_ATTR);
                    Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                    startState.setSwimlane(swimlane);
                }
            }
        }
        List<Element> states = root.elements(TASK_STATE_NODE);
        for (Element node : states) {
            State state = create(node, definition);
            List<Element> stateChilds = node.elements();
            for (Element stateNodeChild : stateChilds) {
                if (TASK_NODE.equals(stateNodeChild.getName())) {
                    String swimlaneName = stateNodeChild.attributeValue(SWIMLANE_ATTR);
                    if (swimlaneName != null && state instanceof SwimlanedNode) {
                        Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                        ((SwimlanedNode) state).setSwimlane(swimlane);
                        String reassign = stateNodeChild.attributeValue(REASSIGN_ATTR);
                        if (reassign != null) {
                            boolean forceReassign = Boolean.parseBoolean(reassign);
                            state.setReassignmentEnabled(forceReassign);
                        }
                    }
                    String duedateAttr = stateNodeChild.attributeValue(DUEDATE_ATTR);
                    if (duedateAttr != null) {
                        state.setTimeOutDueDate(duedateAttr);
                    }
                    List<Element> aaa = stateNodeChild.elements();
                    for (Element a : aaa) {
                        if (EVENT_NODE.equals(a.getName())) {
                            String eventType = a.attributeValue(TYPE_ATTR);
                            List<Element> actionNodes = a.elements();
                            for (Element aa : actionNodes) {
                                if (ACTION_NODE.equals(aa.getName())) {
                                    parseAction(aa, state, eventType);
                                }
                            }
                        }
                    }
                }
                if (TIMER_NODE.equals(stateNodeChild.getName())) {
                    String nameTimer = stateNodeChild.attributeValue(NAME_ATTR);
                    String dueDate = stateNodeChild.attributeValue(DUEDATE_ATTR);
                    if (TIMER_ESCALATION.equals(nameTimer)) {
                        ((TaskState) state).setUseEscalation(true);
                        if (dueDate != null) {
                            ((TaskState) state).setEscalationTime(new TimerDuration(dueDate));
                        }
                    } else if (TIMER_GLOBAL_NAME.equals(nameTimer)) {
                        definition.setTimeOutDueDate(dueDate);
                    } else {
                        state.setHasTimer(true);
                        if (dueDate != null) {
                            state.setDueDate(dueDate);
                        }
                    }
                    List<Element> actionNodes = stateNodeChild.elements();
                    for (Element aa : actionNodes) {
                        if (ACTION_NODE.equals(aa.getName())) {
                            TimerAction timerAction = new TimerAction(null);
                            timerAction.setDelegationClassName(aa.attributeValue(CLASS_ATTR));
                            timerAction.setDelegationConfiguration(aa.getTextTrim());
                            timerAction.setRepeat(stateNodeChild.attributeValue(REPEAT_ATTR));
                            if (TIMER_GLOBAL_NAME.equals(nameTimer)) {
                                definition.setTimeOutAction(timerAction);
                            } else if (TIMER_ESCALATION.equals(nameTimer)) {
                                ((TaskState) state).setEscalationAction(timerAction);
                            } else {
                                ((ITimed) state).setTimerAction(timerAction);
                            }
                        }
                    }
                }
            }
        }
        List<Element> waitStates = root.elements(WAIT_STATE_NODE);
        for (Element node : waitStates) {
            create(node, definition);
            WaitState state = create(node, definition);
            List<Element> stateChilds = node.elements();
            for (Element stateNodeChild : stateChilds) {
                if (TIMER_NODE.equals(stateNodeChild.getName())) {
                    String nameTimer = stateNodeChild.attributeValue(NAME_ATTR);
                    String dueDate = stateNodeChild.attributeValue(DUEDATE_ATTR);
                    if (dueDate != null) {
                        state.setDueDate(dueDate);
                    } else {
                        state.setDueDate(TimerDuration.EMPTY);
                    }
                    List<Element> actionNodes = stateNodeChild.elements();
                    for (Element aa : actionNodes) {
                        if (ACTION_NODE.equals(aa.getName())) {
                            TimerAction timerAction = new TimerAction(null);
                            timerAction.setDelegationClassName(aa.attributeValue(CLASS_ATTR));
                            timerAction.setDelegationConfiguration(aa.getTextTrim());
                            timerAction.setRepeat(stateNodeChild.attributeValue(REPEAT_ATTR));
                            if (TIMER_GLOBAL_NAME.equals(nameTimer)) {
                                definition.setTimeOutAction(timerAction);
                            } else {
                                ((ITimed) state).setTimerAction(timerAction);
                            }
                        }
                    }
                }
            }
        }
        List<Element> forks = root.elements(FORK_NODE);
        for (Element node : forks) {
            create(node, definition);
        }
        List<Element> joins = root.elements(JOIN_NODE);
        for (Element node : joins) {
            create(node, definition);
        }
        List<Element> decisions = root.elements(DECISION_NODE);
        for (Element node : decisions) {
            create(node, definition);
        }
        List<Element> processStates = root.elements(PROCESS_STATE_NODE);
        for (Element node : processStates) {
            Subprocess subprocess = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            List<Element> nodeList = node.elements();
            for (Element childNode : nodeList) {
                if (SUB_PROCESS_NODE.equals(childNode.getName())) {
                    subprocess.setSubProcessName(childNode.attributeValue(NAME_ATTR));
                }
                if (VARIABLE_NODE.equals(childNode.getName())) {
                    VariableMapping variable = new VariableMapping();
                    variable.setProcessVariable(childNode.attributeValue(NAME_ATTR));
                    variable.setSubprocessVariable(childNode.attributeValue(MAPPED_NAME_ATTR));
                    variable.setUsage(childNode.attributeValue(ACCESS_ATTR));
                    variablesList.add(variable);
                }
            }
            subprocess.setVariablesList(variablesList);
        }
        List<Element> multiInstanceStates = root.elements(MULTI_INSTANCE_STATE_NODE);
        for (Element node : multiInstanceStates) {
            MultiInstance multiInstance = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            List<Element> nodeList = node.elements();
            for (Element childNode : nodeList) {
                if (SUB_PROCESS_NODE.equals(childNode.getName())) {
                    multiInstance.setSubProcessName(childNode.attributeValue(NAME_ATTR));
                }
                if (VARIABLE_NODE.equals(childNode.getName())) {
                    VariableMapping variable = new VariableMapping();
                    variable.setProcessVariable(childNode.attributeValue(NAME_ATTR));
                    variable.setSubprocessVariable(childNode.attributeValue(MAPPED_NAME_ATTR));
                    variable.setUsage(childNode.attributeValue(ACCESS_ATTR));
                    variablesList.add(variable);
                }
            }
            multiInstance.setVariablesList(variablesList);
        }
        List<Element> sendMessageNodes = root.elements(SEND_MESSAGE_NODE);
        for (Element node : sendMessageNodes) {
            SendMessageNode messageNode = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            List<Element> nodeList = node.elements();
            for (Element childNode : nodeList) {
                if (VARIABLE_NODE.equals(childNode.getName())) {
                    VariableMapping variable = new VariableMapping();
                    variable.setProcessVariable(childNode.attributeValue(NAME_ATTR));
                    variable.setSubprocessVariable(childNode.attributeValue(MAPPED_NAME_ATTR));
                    variable.setUsage(childNode.attributeValue(ACCESS_ATTR));
                    variablesList.add(variable);
                }
            }
            messageNode.setVariablesList(variablesList);
        }
        List<Element> receiveMessageNodes = root.elements(RECEIVE_MESSAGE_NODE);
        for (Element node : receiveMessageNodes) {
            ReceiveMessageNode messageNode = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            List<Element> nodeList = node.elements();
            for (Element childNode : nodeList) {
                if (VARIABLE_NODE.equals(childNode.getName())) {
                    VariableMapping variable = new VariableMapping();
                    variable.setProcessVariable(childNode.attributeValue(NAME_ATTR));
                    variable.setSubprocessVariable(childNode.attributeValue(MAPPED_NAME_ATTR));
                    variable.setUsage(childNode.attributeValue(ACCESS_ATTR));
                    variablesList.add(variable);
                }
                if (TIMER_NODE.equals(childNode.getName())) {
                    String dueDate = childNode.attributeValue(DUEDATE_ATTR);
                    messageNode.setDueDate(dueDate);
                    List<Element> actionNodes = childNode.elements();
                    for (Element aa : actionNodes) {
                        if (ACTION_NODE.equals(aa.getName())) {
                            TimerAction timerAction = new TimerAction(null);
                            timerAction.setDelegationClassName(aa.attributeValue(CLASS_ATTR));
                            timerAction.setDelegationConfiguration(aa.getTextTrim());
                            timerAction.setRepeat(childNode.attributeValue(REPEAT_ATTR));
                            messageNode.setTimerAction(timerAction);
                        }
                    }
                }
            }
            messageNode.setVariablesList(variablesList);
        }
        List<Element> endStates = root.elements(END_STATE_NODE);
        for (Element node : endStates) {
            create(node, definition);
        }
        List<Transition> tmpTransitions = new ArrayList<Transition>(TRANSITION_TARGETS.keySet());
        for (Transition transition : tmpTransitions) {
            String targetNodeId = TRANSITION_TARGETS.remove(transition);
            Node target = definition.getNodeByIdNotNull(targetNodeId);
            transition.setTarget(target);
        }
        return definition;
    }

    @Override
    public void saveToXML(ProcessDefinition definition, Document document) {
        Element root = document.getRootElement();
        root.addAttribute(NAME_ATTR, definition.getName());
        if (!definition.getDefaultTaskDuedate().startsWith("0 ")) {
            root.addAttribute(DEFAULT_DUEDATE_ATTR, definition.getDefaultTaskDuedate());
        }
        if (definition.isInvalid()) {
            root.addAttribute(INVALID_ATTR, String.valueOf(definition.isInvalid()));
        }
        if (definition.getDescription() != null && definition.getDescription().length() > 0) {
            Element desc = root.addElement(DESCRIPTION_NODE);
            setNodeValue(desc, definition.getDescription());
        }
        List<Swimlane> swimlanes = definition.getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            Element swimlaneElement = writeElement(root, swimlane);
            writeDelegation(swimlaneElement, ASSIGNMENT_NODE, swimlane);
        }
        StartState startState = definition.getFirstChild(StartState.class);
        if (startState != null) {
            Element startStateElement = writeTaskState(root, startState);
            writeTransitions(startStateElement, startState);
        }
        List<Decision> decisions = definition.getChildren(Decision.class);
        for (Decision decision : decisions) {
            writeNode(root, decision, HANDLER_NODE);
        }
        List<TaskState> states = definition.getChildren(TaskState.class);
        for (TaskState state : states) {
            Element stateElement = writeTaskStateWithDuedate(root, state);
            if (state.timerExist()) {
                Element timerElement = stateElement.addElement(TIMER_NODE);
                if (state.getDuration() != null && state.getDuration().hasDuration()) {
                    setAttribute(timerElement, DUEDATE_ATTR, state.getDuration().getDuration());
                }
                if (!state.hasTimeoutTransition() && state.getTimerAction() != null) {
                    if (state.getTimerAction().getRepeat().hasDuration()) {
                        setAttribute(timerElement, REPEAT_ATTR, state.getTimerAction().getRepeat().getDuration());
                    }
                    writeDelegation(timerElement, ACTION_NODE, state.getTimerAction());
                } else {
                    setAttribute(timerElement, TRANSITION_ATTR, PluginConstants.TIMER_TRANSITION_NAME);
                }
            }
            if (state.isUseEscalation()) {
                String timerName = TIMER_ESCALATION;
                TimerDuration escalationDuration = state.getEscalationTime();
                Element timerElement = stateElement.addElement(TIMER_NODE);
                setAttribute(timerElement, NAME_ATTR, timerName);
                if (escalationDuration != null && escalationDuration.hasDuration()) {
                    setAttribute(timerElement, DUEDATE_ATTR, escalationDuration.getDuration());
                }
                TimerAction escalationAction = state.getEscalationAction();
                if (escalationAction != null) {
                    if (escalationAction.getRepeat().hasDuration()) {
                        setAttribute(timerElement, REPEAT_ATTR, escalationAction.getRepeat().getDuration());
                    }
                    writeDelegation(timerElement, ACTION_NODE, escalationAction);
                }
            }
            writeTransitions(stateElement, state);
        }
        List<WaitState> waitStates = definition.getChildren(WaitState.class);
        for (WaitState waitState : waitStates) {
            Element stateElement = writeWaitState(root, waitState);
            writeTransitions(stateElement, waitState);
        }
        List<Fork> forks = definition.getChildren(Fork.class);
        for (ru.runa.gpd.lang.model.Node node : forks) {
            writeNode(root, node, null);
        }
        List<Join> joins = definition.getChildren(Join.class);
        for (ru.runa.gpd.lang.model.Node node : joins) {
            writeNode(root, node, null);
        }
        List<Subprocess> subprocesses = definition.getChildren(Subprocess.class);
        for (Subprocess subprocess : subprocesses) {
            Element processStateElement = writeNode(root, subprocess, null);
            Element subProcessElement = processStateElement.addElement(SUB_PROCESS_NODE);
            setAttribute(subProcessElement, NAME_ATTR, subprocess.getSubProcessName());
            for (VariableMapping variable : subprocess.getVariablesList()) {
                Element variableElement = processStateElement.addElement(VARIABLE_NODE);
                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
            }
        }
        List<SendMessageNode> sendMessageNodes = definition.getChildren(SendMessageNode.class);
        for (SendMessageNode messageNode : sendMessageNodes) {
            Element messageElement = writeNode(root, messageNode, null);
            for (VariableMapping variable : messageNode.getVariablesList()) {
                Element variableElement = messageElement.addElement(VARIABLE_NODE);
                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
            }
        }
        List<ReceiveMessageNode> receiveMessageNodes = definition.getChildren(ReceiveMessageNode.class);
        for (ReceiveMessageNode messageNode : receiveMessageNodes) {
            Element messageElement = writeNode(root, messageNode, null);
            for (VariableMapping variable : messageNode.getVariablesList()) {
                Element variableElement = messageElement.addElement(VARIABLE_NODE);
                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
            }
            if (messageNode.timerExist()) {
                Element timerElement = messageElement.addElement(TIMER_NODE);
                setAttribute(timerElement, DUEDATE_ATTR, messageNode.getDuration().getDuration());
                if (messageNode.getTimerAction() != null) {
                    if (messageNode.getTimerAction().getRepeat().hasDuration()) {
                        setAttribute(timerElement, REPEAT_ATTR, messageNode.getTimerAction().getRepeat().getDuration());
                    }
                    writeDelegation(timerElement, ACTION_NODE, messageNode.getTimerAction());
                } else {
                    setAttribute(timerElement, TRANSITION_ATTR, PluginConstants.TIMER_TRANSITION_NAME);
                }
            }
        }
        EndState endState = definition.getFirstChild(EndState.class);
        if (endState != null) {
            writeElement(root, endState);
        }
    }

    private Element writeNode(Element parent, Node node, String delegationNodeName) {
        Element nodeElement = writeElement(parent, node);
        if (delegationNodeName != null) {
            writeDelegation(nodeElement, delegationNodeName, (Delegable) node);
        }
        writeTransitions(nodeElement, node);
        return nodeElement;
    }

    private Element writeTaskStateWithDuedate(Element parent, TaskState state) {
        Element nodeElement = writeElement(parent, state);
        Element taskElement = nodeElement.addElement(TASK_NODE);
        setAttribute(taskElement, DUEDATE_ATTR, state.getTimeOutDueDate());
        setAttribute(taskElement, NAME_ATTR, state.getName());
        setAttribute(taskElement, SWIMLANE_ATTR, state.getSwimlaneName());
        if (state instanceof State && ((State) state).isReassignmentEnabled()) {
            setAttribute(taskElement, REASSIGN_ATTR, "true");
        }
        for (Action action : state.getActions()) {
            ActionImpl actionImpl = (ActionImpl) action;
            writeEvent(taskElement, new Event(actionImpl.getEventType()), actionImpl);
        }
        return nodeElement;
    }

    private Element writeTaskState(Element parent, SwimlanedNode state) {
        Element nodeElement = writeElement(parent, state);
        Element taskElement = nodeElement.addElement(TASK_NODE);
        setAttribute(taskElement, NAME_ATTR, state.getName());
        setAttribute(taskElement, SWIMLANE_ATTR, state.getSwimlaneName());
        if (state instanceof State && ((State) state).isReassignmentEnabled()) {
            setAttribute(taskElement, REASSIGN_ATTR, "true");
        }
        for (Action action : state.getActions()) {
            ActionImpl actionImpl = (ActionImpl) action;
            writeEvent(taskElement, new Event(actionImpl.getEventType()), actionImpl);
        }
        return nodeElement;
    }

    private Element writeWaitState(Element parent, WaitState state) {
        Element nodeElement = writeElement(parent, state, WAIT_STATE_NODE);
        Element timerElement = nodeElement.addElement(TIMER_NODE);
        setAttribute(timerElement, DUEDATE_ATTR, state.getDueDate());
        if (state.getTimerAction() != null) {
            if (state.getTimerAction().getRepeat().hasDuration()) {
                setAttribute(timerElement, REPEAT_ATTR, state.getTimerAction().getRepeat().getDuration());
            }
            writeDelegation(timerElement, ACTION_NODE, state.getTimerAction());
        }
        setAttribute(timerElement, TRANSITION_ATTR, PluginConstants.TIMER_TRANSITION_NAME);
        return nodeElement;
    }

    private Element writeElement(Element parent, GraphElement element) {
        return writeElement(parent, element, element.getTypeName());
    }

    private Element writeElement(Element parent, GraphElement element, String typeName) {
        Element result = parent.addElement(typeName);
        if (element instanceof Node) {
            setAttribute(result, ID_ATTR, ((Node) element).getNodeId());
        }
        if (element instanceof NamedGraphElement) {
            setAttribute(result, NAME_ATTR, ((NamedGraphElement) element).getName());
        }
        if (element instanceof Describable) {
            String description = ((Describable) element).getDescription();
            if (description != null && description.length() > 0) {
                Element desc = result.addElement(DESCRIPTION_NODE);
                setNodeValue(desc, description);
            }
        }
        return result;
    }

    private void writeTransitions(Element parent, Node node) {
        List<Transition> transitions = node.getLeavingTransitions();
        for (Transition transition : transitions) {
            Element transitionElement = writeElement(parent, transition);
            transitionElement.addAttribute(TO_ATTR, transition.getTarget().getNodeId());
            for (Action action : transition.getActions()) {
                writeDelegation(transitionElement, ACTION_NODE, action);
            }
        }
    }

    private void writeEvent(Element parent, Event event, ActionImpl action) {
        Element eventElement = writeElement(parent, event, EVENT_NODE);
        setAttribute(eventElement, TYPE_ATTR, event.getType());
        writeDelegation(eventElement, ACTION_NODE, action);
    }

    private void writeDelegation(Element parent, String elementName, Delegable delegable) {
        Element delegationElement = parent.addElement(elementName);
        setAttribute(delegationElement, CLASS_ATTR, delegable.getDelegationClassName());
        setNodeValue(delegationElement, delegable.getDelegationConfiguration());
    }
}

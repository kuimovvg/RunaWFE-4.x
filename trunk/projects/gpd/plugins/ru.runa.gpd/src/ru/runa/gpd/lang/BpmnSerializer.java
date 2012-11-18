package ru.runa.gpd.lang;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.ActionImpl;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Describable;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Event;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.State;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.ui.dialog.ErrorDialog;
import ru.runa.gpd.util.TimerDuration;
import ru.runa.gpd.util.XmlUtil;

public class BpmnSerializer extends ProcessSerializer {
    private static final String ROOT_ELEMENT = "definitions";
    private static final String PROCESS_ELEMENT = "process";
    private static final String EXTENSION_ELEMENT = "extensionElements";
    private static final String INVALID_ATTR = "invalid";//
    private static final String ACCESS_ATTR = "access";//
    private static final String END_STATE_NODE = "endEvent";
    private static final String VARIABLE_NODE = "variable";//
    private static final String SUB_PROCESS_NODE = "sub-process";//
    private static final String MAPPED_NAME_ATTR = "mapped-name";//
    private static final String PROCESS_STATE_NODE = "process-state";//
    private static final String MULTI_INSTANCE_STATE_NODE = "multiinstance-state";//
    private static final String DECISION_NODE = "decision";//
    private static final String JOIN_NODE = "join";//
    private static final String FORK_NODE = "fork";//
    private static final String DUEDATE_ATTR = "duedate";//
    private static final String DEFAULT_DUEDATE_ATTR = "default-task-duedate";//
    private static final String REPEAT_ATTR = "repeat";//
    private static final String TIMER_NODE = "timer";//
    private static final String ASSIGNMENT_NODE = "assignment";//
    private static final String TASK_STATE_NODE = "userTask";
    private static final String TASK_NODE = "task";//
    private static final String WAIT_STATE_NODE = "wait-state";//
    private static final String START_STATE_NODE = "startEvent";
    private static final String SWIMLANE_NODE = "swimlane";//
    private static final String REASSIGN_ATTR = "reassign";//
    private static final String CLASS_ATTR = "class";//
    private static final String ACTION_NODE = "action";//
    private static final String EVENT_NODE = "event";//
    private static final String TRANSITION_NODE = "sequenceFlow";
    private static final String TRANSITION_FROM_ATTR = "sourceRef";
    private static final String TRANSITION_TO_ATTR = "targetRef";
    private static final String HANDLER_NODE = "handler";//
    private static final String DOCUMENTATION_NODE = "documentation";
    private static final String NAME_ATTR = "name";//
    private static final String TYPE_ATTR = "type";//
    private static final String ID_ATTR = "id";//
    private static final String SWIMLANE_ATTR = "swimlane";//
    private static final String TRANSITION_ATTR = "transition";//
    private static final String SEND_MESSAGE_NODE = "send-message";//
    private static final String RECEIVE_MESSAGE_NODE = "receive-message";//
    private static final String TIMER_GLOBAL_NAME = "__GLOBAL";//
    private static final String TIMER_ESCALATION = "__ESCALATION";//

    @Override
    public boolean isSupported(Document document) {
        return ROOT_ELEMENT.equals(document.getRootElement().getName());
    }

    @Override
    public Document getInitialProcessDefinitionDocument(String processName) {
        Document document = XmlUtil.createDocument(ROOT_ELEMENT);
        Element definitionsElement = document.getRootElement();
        definitionsElement.addNamespace("", "http://www.omg.org/spec/BPMN/20100524/MODEL");
        definitionsElement.addNamespace("bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI");
        definitionsElement.addNamespace("omgdc", "http://www.omg.org/spec/DD/20100524/DC");
        definitionsElement.addNamespace("omgdi", "http://www.omg.org/spec/DD/20100524/DI");
        Element process = definitionsElement.addElement(PROCESS_ELEMENT);
        process.addAttribute(NAME_ATTR, processName);
        return document;
    }

    @Override
    public void saveToXML(ProcessDefinition definition, Document document) {
        Element definitionsElement = document.getRootElement();
        Element process = definitionsElement.element(PROCESS_ELEMENT);
        process.addAttribute(NAME_ATTR, definition.getName());
        //        if (!definition.getDefaultTaskDuedate().startsWith("0 ")) {
        //            process.addAttribute(DEFAULT_DUEDATE_ATTR, definition.getDefaultTaskDuedate());
        //        }
        //        if (definition.isInvalid()) {
        //            process.addAttribute(INVALID_ATTR, String.valueOf(definition.isInvalid()));
        //        }
        if (definition.getDescription() != null && definition.getDescription().length() > 0) {
            Element desc = process.addElement(DOCUMENTATION_NODE);
            setNodeValue(desc, definition.getDescription());
        }
        List<Swimlane> swimlanes = definition.getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            Element swimlaneElement = writeElement(process, swimlane, SWIMLANE_NODE);
            writeDelegation(swimlaneElement, ASSIGNMENT_NODE, swimlane);
        }
        StartState startState = definition.getFirstChild(StartState.class);
        if (startState != null) {
            writeTaskState(process, startState, START_STATE_NODE);
            writeTransitions(process, startState);
        }
        //        List<Decision> decisions = definition.getChildren(Decision.class);
        //        for (Decision decision : decisions) {
        //            writeNode(process, decision, HANDLER_NODE);
        //        }
        List<TaskState> states = definition.getChildren(TaskState.class);
        for (TaskState state : states) {
            Element stateElement = writeTaskState(process, state, TASK_STATE_NODE);
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
            writeTransitions(process, state);
        }
        //        List<WaitState> waitStates = definition.getChildren(WaitState.class);
        //        for (WaitState waitState : waitStates) {
        //            Element stateElement = writeWaitState(process, waitState);
        //            writeTransitions(stateElement, waitState);
        //        }
        //        List<Fork> forks = definition.getChildren(Fork.class);
        //        for (ru.runa.gpd.lang.model.Node node : forks) {
        //            writeNode(process, node, null);
        //        }
        //        List<Join> joins = definition.getChildren(Join.class);
        //        for (ru.runa.gpd.lang.model.Node node : joins) {
        //            writeNode(process, node, null);
        //        }
        //        List<Subprocess> subprocesses = definition.getChildren(Subprocess.class);
        //        for (Subprocess subprocess : subprocesses) {
        //            Element processStateElement = writeNode(process, subprocess, null);
        //            Element subProcessElement = processStateElement.addElement(SUB_PROCESS_NODE);
        //            setAttribute(subProcessElement, NAME_ATTR, subprocess.getSubProcessName());
        //            for (VariableMapping variable : subprocess.getVariablesList()) {
        //                Element variableElement = processStateElement.addElement(VARIABLE_NODE);
        //                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
        //                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
        //                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
        //            }
        //        }
        //        List<SendMessageNode> sendMessageNodes = definition.getChildren(SendMessageNode.class);
        //        for (SendMessageNode messageNode : sendMessageNodes) {
        //            Element messageElement = writeNode(process, messageNode, null);
        //            for (VariableMapping variable : messageNode.getVariablesList()) {
        //                Element variableElement = messageElement.addElement(VARIABLE_NODE);
        //                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
        //                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
        //                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
        //            }
        //        }
        //        List<ReceiveMessageNode> receiveMessageNodes = definition.getChildren(ReceiveMessageNode.class);
        //        for (ReceiveMessageNode messageNode : receiveMessageNodes) {
        //            Element messageElement = writeNode(process, messageNode, null);
        //            for (VariableMapping variable : messageNode.getVariablesList()) {
        //                Element variableElement = messageElement.addElement(VARIABLE_NODE);
        //                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
        //                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
        //                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
        //            }
        //            if (messageNode.timerExist()) {
        //                Element timerElement = messageElement.addElement(TIMER_NODE);
        //                setAttribute(timerElement, DUEDATE_ATTR, messageNode.getDuration().getDuration());
        //                if (messageNode.getTimerAction() != null) {
        //                    if (messageNode.getTimerAction().getRepeat().hasDuration()) {
        //                        setAttribute(timerElement, REPEAT_ATTR, messageNode.getTimerAction().getRepeat().getDuration());
        //                    }
        //                    writeDelegation(timerElement, ACTION_NODE, messageNode.getTimerAction());
        //                } else {
        //                    setAttribute(timerElement, TRANSITION_ATTR, PluginConstants.TIMER_TRANSITION_NAME);
        //                }
        //            }
        //        }
        EndState endState = definition.getFirstChild(EndState.class);
        if (endState != null) {
            writeElement(process, endState, END_STATE_NODE);
        }
        // as gpd.xml
        Element diagramElement = definitionsElement.addElement("bpmndi:BPMNDiagram");
        diagramElement.addAttribute(ID_ATTR, "test");
    }

    //    private Element writeNode(Element parent, Node node, String delegationNodeName) {
    //        Element nodeElement = writeElement(parent, node);
    //        if (delegationNodeName != null) {
    //            writeDelegation(nodeElement, delegationNodeName, (Delegable) node);
    //        }
    //        writeTransitions(nodeElement, node);
    //        return nodeElement;
    //    }
    private Element writeTaskState(Element parent, SwimlanedNode state, String typeName) {
        Element nodeElement = writeElement(parent, state, typeName);
        setAttribute(nodeElement, SWIMLANE_ATTR, state.getSwimlaneName());
        if (state instanceof State && ((State) state).isReassignmentEnabled()) {
            setAttribute(nodeElement, REASSIGN_ATTR, "true");
        }
        //        for (Action action : state.getActions()) {
        //            ActionImpl actionImpl = (ActionImpl) action;
        //            writeEvent(taskElement, new Event(actionImpl.getEventType()), actionImpl);
        //        }
        return nodeElement;
    }

    //    private Element writeWaitState(Element parent, WaitState state) {
    //        Element nodeElement = writeElement(parent, state, WAIT_STATE_NODE);
    //        Element timerElement = nodeElement.addElement(TIMER_NODE);
    //        setAttribute(timerElement, DUEDATE_ATTR, state.getDueDate());
    //        if (state.getTimerAction() != null) {
    //            if (state.getTimerAction().getRepeat().hasDuration()) {
    //                setAttribute(timerElement, REPEAT_ATTR, state.getTimerAction().getRepeat().getDuration());
    //            }
    //            writeDelegation(timerElement, ACTION_NODE, state.getTimerAction());
    //        }
    //        setAttribute(timerElement, TRANSITION_ATTR, PluginConstants.TIMER_TRANSITION_NAME);
    //        return nodeElement;
    //    }
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
                Element desc = result.addElement(DOCUMENTATION_NODE);
                setNodeValue(desc, description);
            }
        }
        return result;
    }

    private void writeTransitions(Element parent, Node node) {
        List<Transition> transitions = node.getLeavingTransitions();
        for (Transition transition : transitions) {
            Element transitionElement = parent.addElement(TRANSITION_NODE);
            transitionElement.addAttribute(ID_ATTR, transition.getName());
            transitionElement.addAttribute(TRANSITION_FROM_ATTR, transition.getSource().getNodeId());
            transitionElement.addAttribute(TRANSITION_TO_ATTR, transition.getTarget().getNodeId());
            //            for (Action action : transition.getActions()) {
            //                writeDelegation(transitionElement, ACTION_NODE, action);
            //            }
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

    @Override
    public void validateProcessDefinitionXML(IFile file) {
        //        try {
        //            XmlUtil.parseWithXSDValidation(getClass().getResourceAsStream("/schema/" + XSD_FILE_NAME));
        //        } catch (Exception e) {
        //            throw new RuntimeException(e);
        //        } TODO
    }

    private <T extends GraphElement> T create(Element node, GraphElement parent, Class<? extends GraphElement> nodeClass) {
        GraphElement element = NodeRegistry.getNodeTypeDefinition(nodeClass).createElement(parent);
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
            if (DOCUMENTATION_NODE.equals(childNode.getName())) {
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
        }
        return (T) element;
    }

    private void parseAction(Element node, GraphElement parent, String eventType) {
        ActionImpl action = NodeRegistry.getNodeTypeDefinition(Action.class).createElement(parent);
        action.setDelegationClassName(node.attributeValue(CLASS_ATTR));
        action.setDelegationConfiguration(node.getTextTrim());
        parent.addAction(action, -1);
        action.setEventType(eventType);
    }

    @Override
    public ProcessDefinition parseXML(Document document) {
        Element definitionsElement = document.getRootElement();
        Element process = definitionsElement.element(PROCESS_ELEMENT);
        ProcessDefinition definition = create(process, null, ProcessDefinition.class);
        //definition.setDefaultTaskDuedate(root.attributeValue(DEFAULT_DUEDATE_ATTR));
        List<Element> swimlanes = process.elements(SWIMLANE_NODE);
        for (Element node : swimlanes) {
            create(node, definition, Swimlane.class);
        }
        List<Element> startStates = process.elements(START_STATE_NODE);
        if (startStates.size() > 0) {
            if (startStates.size() > 1) {
                ErrorDialog.open(Localization.getString("model.validation.multipleStartStatesNotAllowed"));
            }
            Element node = startStates.get(0);
            StartState startState = create(node, definition, StartState.class);
            String swimlaneName = node.attributeValue(SWIMLANE_ATTR);
            Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
            startState.setSwimlane(swimlane);
        }
        List<Element> states = process.elements(TASK_STATE_NODE);
        for (Element node : states) {
            State state = create(node, definition, TaskState.class);
            String swimlaneName = node.attributeValue(SWIMLANE_ATTR);
            if (swimlaneName != null && state instanceof SwimlanedNode) {
                Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                ((SwimlanedNode) state).setSwimlane(swimlane);
                String reassign = node.attributeValue(REASSIGN_ATTR);
                if (reassign != null) {
                    boolean forceReassign = Boolean.parseBoolean(reassign);
                    state.setReassignmentEnabled(forceReassign);
                }
            }
            List<Element> stateChilds = node.elements();
            for (Element stateNodeChild : stateChilds) {
                if (TASK_NODE.equals(stateNodeChild.getName())) {
                    //                    String duedateAttr = stateNodeChild.attributeValue(DUEDATE_ATTR);
                    //                    if (duedateAttr != null) {
                    //                        state.setTimeOutDueDate(duedateAttr);
                    //                    }
                    //                    List<Element> aaa = stateNodeChild.elements();
                    //                    for (Element a : aaa) {
                    //                        if (EVENT_NODE.equals(a.getName())) {
                    //                            String eventType = a.attributeValue(TYPE_ATTR);
                    //                            List<Element> actionNodes = a.elements();
                    //                            for (Element aa : actionNodes) {
                    //                                if (ACTION_NODE.equals(aa.getName())) {
                    //                                    parseAction(aa, state, eventType);
                    //                                }
                    //                            }
                    //                        }
                    //                    }
                }
                //                if (TIMER_NODE.equals(stateNodeChild.getName())) {
                //                    String nameTimer = stateNodeChild.attributeValue(NAME_ATTR);
                //                    String dueDate = stateNodeChild.attributeValue(DUEDATE_ATTR);
                //                    if (TIMER_ESCALATION.equals(nameTimer)) {
                //                        ((TaskState) state).setUseEscalation(true);
                //                        if (dueDate != null) {
                //                            ((TaskState) state).setEscalationTime(new TimerDuration(dueDate));
                //                        }
                //                    } else if (TIMER_GLOBAL_NAME.equals(nameTimer)) {
                //                        definition.setTimeOutDueDate(dueDate);
                //                    } else {
                //                        state.setHasTimer(true);
                //                        if (dueDate != null) {
                //                            state.setDueDate(dueDate);
                //                        }
                //                    }
                //                    List<Element> actionNodes = stateNodeChild.elements();
                //                    for (Element aa : actionNodes) {
                //                        if (ACTION_NODE.equals(aa.getName())) {
                //                            TimerAction timerAction = new TimerAction(null);
                //                            timerAction.setDelegationClassName(aa.attributeValue(CLASS_ATTR));
                //                            timerAction.setDelegationConfiguration(aa.getTextTrim());
                //                            timerAction.setRepeat(stateNodeChild.attributeValue(REPEAT_ATTR));
                //                            if (TIMER_GLOBAL_NAME.equals(nameTimer)) {
                //                                definition.setTimeOutAction(timerAction);
                //                            } else if (TIMER_ESCALATION.equals(nameTimer)) {
                //                                ((TaskState) state).setEscalationAction(timerAction);
                //                            } else {
                //                                ((ITimed) state).setTimerAction(timerAction);
                //                            }
                //                        }
                //                    }
                //                }
            }
        }
        List<Element> endStates = process.elements(END_STATE_NODE);
        for (Element node : endStates) {
            create(node, definition, EndState.class);
        }
        List<Element> transitions = process.elements(TRANSITION_NODE);
        for (Element transitionElement : transitions) {
            String transitionId = transitionElement.attributeValue(ID_ATTR);
            String sourceNodeId = transitionElement.attributeValue(TRANSITION_FROM_ATTR);
            String targetNodeId = transitionElement.attributeValue(TRANSITION_TO_ATTR);
            Node source = definition.getNodeByIdNotNull(sourceNodeId);
            Node target = definition.getNodeByIdNotNull(targetNodeId);
            Transition transition = NodeRegistry.getNodeTypeDefinition(Transition.class).createElement(source);
            transition.setName(transitionId);
            transition.setTarget(target);
            source.addLeavingTransition(transition);
        }
        return definition;
    }
}

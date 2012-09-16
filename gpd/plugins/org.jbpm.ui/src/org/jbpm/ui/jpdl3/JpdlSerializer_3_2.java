package ru.runa.bpm.ui.jpdl3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import ru.runa.bpm.ui.ElementSerializer;
import ru.runa.bpm.ui.JpdlSerializer;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.Decision;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.common.model.Describable;
import ru.runa.bpm.ui.common.model.EndState;
import ru.runa.bpm.ui.common.model.Fork;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ITimed;
import ru.runa.bpm.ui.common.model.Join;
import ru.runa.bpm.ui.common.model.NamedGraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.State;
import ru.runa.bpm.ui.common.model.Subprocess;
import ru.runa.bpm.ui.common.model.Swimlane;
import ru.runa.bpm.ui.common.model.SwimlanedNode;
import ru.runa.bpm.ui.common.model.TimerAction;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.common.model.WaitState;
import ru.runa.bpm.ui.dialog.ErrorDialog;
import ru.runa.bpm.ui.jpdl3.model.ActionImpl;
import ru.runa.bpm.ui.jpdl3.model.ActionNode;
import ru.runa.bpm.ui.jpdl3.model.Event;
import ru.runa.bpm.ui.jpdl3.model.MailNode;
import ru.runa.bpm.ui.jpdl3.model.MultiInstance;
import ru.runa.bpm.ui.jpdl3.model.ReceiveMessageNode;
import ru.runa.bpm.ui.jpdl3.model.SendMessageNode;
import ru.runa.bpm.ui.jpdl3.model.StartState;
import ru.runa.bpm.ui.jpdl3.model.TaskState;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.SimpleErrorHandler;
import ru.runa.bpm.ui.util.TimerDuration;
import ru.runa.bpm.ui.util.VariableMapping;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JpdlSerializer_3_2 extends JpdlSerializer {

    // private static final String TIMER_LOCAL_NAME = "__LOCAL";

    private static final String TIMER_GLOBAL_NAME = "__GLOBAL";
    private static final String TIMER_ESCALATION = "__ESCALATION";

    private static final String NAMESPACE = "urn:jbpm.org:jpdl-3.2";

    private static final String XSD_FILE_NAME = "jpdl-3.2.xsd";

    private static boolean validationEnabled = true;

    public static void setValidationEnabled(boolean validationEnabled) {
        JpdlSerializer_3_2.validationEnabled = validationEnabled;
    }

    @Override
    public Document getInitialProcessDefinitionDocument(String processName) throws ParserConfigurationException {
        Document document = XmlUtil.createDocument("process-definition", null, null);
        setAttribute(document.getDocumentElement(), "name", processName);
        setAttribute(document.getDocumentElement(), XMLConstants.XMLNS_ATTRIBUTE, NAMESPACE);
        return document;
    }

    @Override
    public void validateProcessDefinitionXML(IFile file) throws SAXException {
        if (!validationEnabled) {
            return;
        }
        // TODO move to XmlUtil
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/validation", true);
            factory.setValidating(true);
            // DTD validation
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // XSD validation
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", getClass().getResourceAsStream("/schema/" + XSD_FILE_NAME));
            factory.setFeature("http://apache.org/xml/features/validation/schema", true);
            factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
            factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(SimpleErrorHandler.getInstance());
            documentBuilder.parse(file.getContents());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isSupported(Document document) {
        return NAMESPACE.equals(document.getDocumentElement().getNamespaceURI());
    }

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
    private static final String ACTION_NODE_NODE = "node";
    private static final String MAIL_NODE = "mail-node";
    private static final String START_STATE_NODE = "start-state";
    private static final String SWIMLANE_NODE = "swimlane";
    private static final String REASSIGN_ATTR = "reassign";
    private static final String END_TASKS_ATTR = "end-tasks";
    private static final String TO_ATTR = "to";
    private static final String CLASS_ATTR = "class";
    private static final String ACTION_NODE = "action";
    private static final String EVENT_NODE = "event";
    private static final String TRANSITION_NODE = "transition";
    private static final String HANDLER_NODE = "handler";
    private static final String DESCRIPTION_NODE = "description";
    private static final String NAME_ATTR = "name";
    private static final String SEND_MESSAGE_NODE = "send-message";
    private static final String RECEIVE_MESSAGE_NODE = "receive-message";

    private <T extends GraphElement> T create(Node node, GraphElement parent) {
        return create(node, parent, node.getNodeName());
    }

    @SuppressWarnings("unchecked")
    private <T extends GraphElement> T create(Node node, GraphElement parent, String typeName) {
        GraphElement element = JpdlVersionRegistry.getElementTypeDefinition(jpdlVersion, typeName).createElement();
        if (parent != null) {
            parent.addChild(element);
        }
        if (element instanceof NamedGraphElement) {
            ((NamedGraphElement) element).setName(getAttribute(node, NAME_ATTR));
        }
        NodeList nodeList = node.getChildNodes();
        for (int j = 0; j < nodeList.getLength(); j++) {
            Node childNode = nodeList.item(j);
            if (DESCRIPTION_NODE.equals(childNode.getNodeName())) {
                ((Describable) element).setDescription(getTextContent(childNode));
            }
            if (HANDLER_NODE.equals(childNode.getNodeName()) || ASSIGNMENT_NODE.equals(childNode.getNodeName())) {
                ((Delegable) element).setDelegationClassName(getAttribute(childNode, CLASS_ATTR));
                element.setDelegationConfiguration(getTextContent(childNode));
            }
            if (ACTION_NODE.equals(childNode.getNodeName())) {
                // only transition actions loaded here
                String eventType;
                if (element instanceof Transition) {
                    eventType = Event.TRANSITION;
                } else if (element instanceof ActionNode) {
                    eventType = Event.NODE_ACTION;
                } else {
                    throw new RuntimeException("Unexpected action in XML, context of " + element);
                }
                parseAction(childNode, element, eventType);
            }
            if (TRANSITION_NODE.equals(childNode.getNodeName())) {
                parseTransition(childNode, element);
            }
        }
        return (T) element;
    }

    private void parseTransition(Node node, GraphElement parent) {
        Transition transition = create(node, parent);
        String targetName = getAttribute(node, TO_ATTR);
        TRANSITION_TARGETS.put(transition, targetName);
    }

    private void parseAction(Node node, GraphElement parent, String eventType) {
        ActionImpl action = JpdlVersionRegistry.getElementTypeDefinition(jpdlVersion, ACTION_NODE).createElement();
        action.setDelegationClassName(getAttribute(node, CLASS_ATTR));
        action.setDelegationConfiguration(getTextContent(node));
        parent.addAction(action, -1);
        action.setEventType(eventType);
    }

    private static Map<Transition, String> TRANSITION_TARGETS = new HashMap<Transition, String>();

    @Override
    public ProcessDefinition parseXML(Document document) {
        TRANSITION_TARGETS.clear();
        ProcessDefinition definition = create(document.getDocumentElement(), null);
        definition.setDefaultTaskDuedate(document.getDocumentElement().getAttribute(DEFAULT_DUEDATE_ATTR));

        NodeList swimlanes = document.getElementsByTagName(SWIMLANE_NODE);
        for (int i = 0; i < swimlanes.getLength(); i++) {
            Node node = swimlanes.item(i);
            create(node, definition);
        }

        NodeList startStates = document.getElementsByTagName(START_STATE_NODE);
        if (startStates.getLength() > 0) {
            if (startStates.getLength() > 1) {
                ErrorDialog.open(Messages.getString("model.validation.multipleStartStatesNotAllowed"));
            }
            Node node = startStates.item(0);
            StartState startState = create(node, definition);
            NodeList stateChilds = node.getChildNodes();
            for (int j = 0; j < stateChilds.getLength(); j++) {
                Node stateNodeChild = stateChilds.item(j);
                if (TASK_NODE.equals(stateNodeChild.getNodeName())) {
                    String swimlaneName = getAttribute(stateNodeChild, SWIMLANE_NODE);
                    Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                    startState.setSwimlane(swimlane);
                }
            }
        }

        NodeList actionNodeNodes = document.getElementsByTagName(ACTION_NODE_NODE);
        for (int i = 0; i < actionNodeNodes.getLength(); i++) {
            Node node = actionNodeNodes.item(i);
            ActionNode actionNode = create(node, definition);
            NodeList aaa = node.getChildNodes();
            for (int k = 0; k < aaa.getLength(); k++) {
                Node a = aaa.item(k);
                if (EVENT_NODE.equals(a.getNodeName())) {
                    String eventType = getAttribute(a, "type");
                    NodeList actionNodes = a.getChildNodes();
                    for (int l = 0; l < actionNodes.getLength(); l++) {
                        Node aa = actionNodes.item(l);
                        if (ACTION_NODE.equals(aa.getNodeName())) {
                            parseAction(aa, actionNode, eventType);
                        }
                    }
                }
            }
        }

        NodeList states = document.getElementsByTagName(TASK_STATE_NODE);
        for (int i = 0; i < states.getLength(); i++) {
            Node node = states.item(i);
            NodeList nodeList = node.getChildNodes();

            int transitionsCount = 0;
            boolean hasTimeOutTransition = false;
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node childNode = nodeList.item(j);
                if (TRANSITION_NODE.equals(childNode.getNodeName())) {
                    String transitionName = getAttribute(childNode, NAME_ATTR);
                    if (PluginConstants.TIMER_TRANSITION_NAME.equals(transitionName)) {
                        hasTimeOutTransition = true;
                    }
                    transitionsCount++;
                }
            }
            GraphElement state;
            if (transitionsCount == 1 && hasTimeOutTransition) {
                state = create(node, definition, "waitState");
            } else {
                state = create(node, definition);
            }

            // TODO use SAX parser instead
            NodeList stateChilds = node.getChildNodes();
            for (int j = 0; j < stateChilds.getLength(); j++) {
                Node stateNodeChild = stateChilds.item(j);
                if (TASK_NODE.equals(stateNodeChild.getNodeName())) {
                    String swimlaneName = getAttribute(stateNodeChild, SWIMLANE_NODE);
                    if (swimlaneName != null && state instanceof SwimlanedNode) {
                        Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                        ((SwimlanedNode) state).setSwimlane(swimlane);
                        String reassign = getAttribute(stateNodeChild, REASSIGN_ATTR);
                        if (reassign != null) {
                            boolean forceReassign = Boolean.parseBoolean(reassign);
                            ((State) state).setReassignmentEnabled(forceReassign);
                        }
                    }
                    String duedate_attr = getAttribute(stateNodeChild, DUEDATE_ATTR);
                    if (duedate_attr != null) {
                        ((State) state).setTimeOutDueDate(duedate_attr);
                    }

                    NodeList aaa = stateNodeChild.getChildNodes();
                    for (int k = 0; k < aaa.getLength(); k++) {
                        Node a = aaa.item(k);
                        if (EVENT_NODE.equals(a.getNodeName())) {
                            String eventType = getAttribute(a, "type");
                            NodeList actionNodes = a.getChildNodes();
                            for (int l = 0; l < actionNodes.getLength(); l++) {
                                Node aa = actionNodes.item(l);
                                if (ACTION_NODE.equals(aa.getNodeName())) {
                                    parseAction(aa, state, eventType);
                                }
                            }
                        }
                    }
                }
                if (TIMER_NODE.equals(stateNodeChild.getNodeName())) {
                    String nameTimer = getAttribute(stateNodeChild, NAME_ATTR);
                    String dueDate = getAttribute(stateNodeChild, DUEDATE_ATTR);

                    if (TIMER_ESCALATION.equals(nameTimer)) {
                        ((TaskState) state).setUseEscalation(true);
                        if (dueDate != null) {
                            ((TaskState) state).setEscalationTime(new TimerDuration(dueDate));
                        }
                    } else if (TIMER_GLOBAL_NAME.equals(nameTimer)) {
                        definition.setTimeOutDueDate(dueDate);
                    } else {
                        ((State) state).setHasTimer(true);
                        if (dueDate != null) {
                            ((State) state).setDueDate(dueDate);
                        }
                    }

                    NodeList actionNodes = stateNodeChild.getChildNodes();
                    for (int l = 0; l < actionNodes.getLength(); l++) {
                        Node aa = actionNodes.item(l);
                        if (ACTION_NODE.equals(aa.getNodeName())) {
                            TimerAction timerAction = new TimerAction(null);
                            timerAction.setDelegationClassName(getAttribute(aa, CLASS_ATTR));
                            timerAction.setDelegationConfiguration(getTextContent(aa));
                            timerAction.setRepeat(getAttribute(stateNodeChild, REPEAT_ATTR));
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

        NodeList mailNodes = document.getElementsByTagName(MAIL_NODE);
        for (int i = 0; i < mailNodes.getLength(); i++) {
            Node node = mailNodes.item(i);
            MailNode mailNode = create(node, definition);
            mailNode.setRecipient(getAttribute(node, "to"));
            NodeList mailNodeChilds = node.getChildNodes();
            for (int j = 0; j < mailNodeChilds.getLength(); j++) {
                Node mailNodeChild = mailNodeChilds.item(i);
                if ("body".equals(mailNodeChild.getNodeName())) {
                    mailNode.setMailBody(getTextContent(mailNodeChild));
                }
                if ("subject".equals(mailNodeChild.getNodeName())) {
                    mailNode.setSubject(getTextContent(mailNodeChild));
                }
            }
        }

        NodeList forks = document.getElementsByTagName(FORK_NODE);
        for (int i = 0; i < forks.getLength(); i++) {
            Node node = forks.item(i);
            create(node, definition);
        }

        NodeList joins = document.getElementsByTagName(JOIN_NODE);
        for (int i = 0; i < joins.getLength(); i++) {
            Node node = joins.item(i);
            create(node, definition);
        }

        NodeList decisions = document.getElementsByTagName(DECISION_NODE);
        for (int i = 0; i < decisions.getLength(); i++) {
            Node node = decisions.item(i);
            create(node, definition);
        }

        NodeList processStates = document.getElementsByTagName(PROCESS_STATE_NODE);
        for (int i = 0; i < processStates.getLength(); i++) {
            Node node = processStates.item(i);
            Subprocess subprocess = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            NodeList nodeList = node.getChildNodes();
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node childNode = nodeList.item(j);
                if (SUB_PROCESS_NODE.equals(childNode.getNodeName())) {
                    subprocess.setSubProcessName(getAttribute(childNode, NAME_ATTR));
                }
                if (VARIABLE_NODE.equals(childNode.getNodeName())) {
                    VariableMapping variable = new VariableMapping();
                    variable.setProcessVariable(getAttribute(childNode, NAME_ATTR));
                    variable.setSubprocessVariable(getAttribute(childNode, MAPPED_NAME_ATTR));
                    variable.setUsage(getAttribute(childNode, ACCESS_ATTR));
                    variablesList.add(variable);
                }
            }
            subprocess.setVariablesList(variablesList);
        }

        NodeList multiInstanceStates = document.getElementsByTagName(MULTI_INSTANCE_STATE_NODE);
        for (int i = 0; i < multiInstanceStates.getLength(); i++) {
            Node node = multiInstanceStates.item(i);
            MultiInstance multiInstance = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            NodeList nodeList = node.getChildNodes();
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node childNode = nodeList.item(j);
                if (SUB_PROCESS_NODE.equals(childNode.getNodeName())) {
                    multiInstance.setSubProcessName(getAttribute(childNode, NAME_ATTR));
                }
                if (VARIABLE_NODE.equals(childNode.getNodeName())) {
                    VariableMapping variable = new VariableMapping();
                    variable.setProcessVariable(getAttribute(childNode, NAME_ATTR));
                    variable.setSubprocessVariable(getAttribute(childNode, MAPPED_NAME_ATTR));
                    variable.setUsage(getAttribute(childNode, ACCESS_ATTR));
                    variablesList.add(variable);
                }
            }
            multiInstance.setVariablesList(variablesList);
        }

        NodeList sendMessageNodes = document.getElementsByTagName(SEND_MESSAGE_NODE);
        for (int i = 0; i < sendMessageNodes.getLength(); i++) {
            Node node = sendMessageNodes.item(i);
            SendMessageNode messageNode = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            NodeList nodeList = node.getChildNodes();
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node childNode = nodeList.item(j);
                if (VARIABLE_NODE.equals(childNode.getNodeName())) {
                    VariableMapping variable = new VariableMapping();
                    variable.setProcessVariable(getAttribute(childNode, NAME_ATTR));
                    variable.setSubprocessVariable(getAttribute(childNode, MAPPED_NAME_ATTR));
                    variable.setUsage(getAttribute(childNode, ACCESS_ATTR));
                    variablesList.add(variable);
                }
            }
            messageNode.setVariablesList(variablesList);
        }

        NodeList receiveMessageNodes = document.getElementsByTagName(RECEIVE_MESSAGE_NODE);
        for (int i = 0; i < receiveMessageNodes.getLength(); i++) {
            Node node = receiveMessageNodes.item(i);
            ReceiveMessageNode messageNode = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            NodeList nodeList = node.getChildNodes();
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node childNode = nodeList.item(j);
                if (VARIABLE_NODE.equals(childNode.getNodeName())) {
                    VariableMapping variable = new VariableMapping();
                    variable.setProcessVariable(getAttribute(childNode, NAME_ATTR));
                    variable.setSubprocessVariable(getAttribute(childNode, MAPPED_NAME_ATTR));
                    variable.setUsage(getAttribute(childNode, ACCESS_ATTR));
                    variablesList.add(variable);
                }
                if (TIMER_NODE.equals(childNode.getNodeName())) {
                    String dueDate = getAttribute(childNode, DUEDATE_ATTR);
                    messageNode.setDueDate(dueDate);
                    NodeList actionNodes = childNode.getChildNodes();
                    for (int l = 0; l < actionNodes.getLength(); l++) {
                        Node aa = actionNodes.item(l);
                        if (ACTION_NODE.equals(aa.getNodeName())) {
                            TimerAction timerAction = new TimerAction(null);
                            timerAction.setDelegationClassName(getAttribute(aa, CLASS_ATTR));
                            timerAction.setDelegationConfiguration(getTextContent(aa));
                            timerAction.setRepeat(getAttribute(childNode, REPEAT_ATTR));
                            messageNode.setTimerAction(timerAction);
                        }
                    }
                }
            }
            messageNode.setVariablesList(variablesList);
        }

        NodeList endStates = document.getElementsByTagName(END_STATE_NODE);
        for (int i = 0; i < endStates.getLength(); i++) {
            Node node = endStates.item(i);
            create(node, definition);
        }

        for (ElementSerializer elementSerializer : JpdlVersionRegistry.getElementSerializers(jpdlVersion)) {
            elementSerializer.parseFromXml(document, definition, TRANSITION_TARGETS);
        }

        List<Transition> tmpTransitions = new ArrayList<Transition>(TRANSITION_TARGETS.keySet());
        for (Transition transition : tmpTransitions) {
            String targetName = TRANSITION_TARGETS.remove(transition);
            ru.runa.bpm.ui.common.model.Node target = definition.getNodeByNameNotNull(targetName);
            transition.setTarget(target);
        }

        return definition;
    }

    @Override
    public void saveToXML(ProcessDefinition definition, Document document) {
        Element root = document.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        if (nodes.getLength() > 0) {
            for (int i = nodes.getLength() - 1; i != 0; i--) {
                root.removeChild(nodes.item(i));
            }
        }
        setAttribute(root, NAME_ATTR, definition.getName());
        if (!definition.getDefaultTaskDuedate().startsWith("0 ")) {
            setAttribute(root, DEFAULT_DUEDATE_ATTR, definition.getDefaultTaskDuedate());
        } else {
            root.removeAttribute(DEFAULT_DUEDATE_ATTR);
        }

        if (definition.isInvalid()) {
            setAttribute(root, "invalid", "true");
        } else {
            root.removeAttribute("invalid");
        }

        if (definition.getDescription() != null && definition.getDescription().length() > 0) {
            Element desc = document.createElement(DESCRIPTION_NODE);
            setNodeValue(desc, definition.getDescription());
            root.appendChild(desc);
        }

        List<Swimlane> swimlanes = definition.getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            Element swimlaneElement = writeElement(document, root, swimlane);
            writeDelegation(document, swimlaneElement, ASSIGNMENT_NODE, swimlane);
        }

        StartState startState = definition.getFirstChild(StartState.class);
        if (startState != null) {
            Element startStateElement = writeTaskState(document, root, startState);
            writeTransitions(document, startStateElement, startState);
        }

        List<ActionNode> actionNodeNodes = definition.getChildren(ActionNode.class);
        for (ActionNode actionNode : actionNodeNodes) {
            Element actionNodeElement = writeNode(document, root, actionNode, null);
            for (Action action : actionNode.getActions()) {
                ActionImpl actionImpl = (ActionImpl) action;
                if (!Event.NODE_ACTION.equals(actionImpl.getEventType())) {
                    writeEvent(document, actionNodeElement, new Event(actionImpl.getEventType()), actionImpl);
                }
            }
        }

        List<Decision> decisions = definition.getChildren(Decision.class);
        for (Decision decision : decisions) {
            writeNode(document, root, decision, HANDLER_NODE);
        }

        List<TaskState> states = definition.getChildren(TaskState.class);
        for (TaskState state : states) {
            Element stateElement = writeTaskStateWithDuedate(document, root, state);
            if (state.timerExist()) {
                Element timerElement = document.createElement(TIMER_NODE);
                if (state.getDuration() != null && state.getDuration().hasDuration()) {
                    setAttribute(timerElement, DUEDATE_ATTR, state.getDuration().getDuration());
                }
                if (!state.hasTimeoutTransition() && state.getTimerAction() != null) {
                    if (state.getTimerAction().getRepeat().hasDuration()) {
                        setAttribute(timerElement, REPEAT_ATTR, state.getTimerAction().getRepeat().getDuration());
                    }
                    writeDelegation(document, timerElement, ACTION_NODE, state.getTimerAction());
                } else {
                    setAttribute(timerElement, TRANSITION_NODE, PluginConstants.TIMER_TRANSITION_NAME);
                }
                stateElement.appendChild(timerElement);
            }
            if (state.isUseEscalation()) {
                // boolean escalationEnabled = DesignerPlugin.getPrefBoolean(PrefConstants.P_TASKS_TIMEOUT_ENABLED);
                // if (escalationEnabled) {
                String timerName = TIMER_ESCALATION;
                TimerDuration escalationDuration = state.getEscalationTime();
                Element timerElement = document.createElement(TIMER_NODE);
                setAttribute(timerElement, NAME_ATTR, timerName);
                if (escalationDuration != null && escalationDuration.hasDuration()) {
                    setAttribute(timerElement, DUEDATE_ATTR, escalationDuration.getDuration());
                }
                TimerAction escalationAction = state.getEscalationAction();
                if (escalationAction != null) {
                    if (escalationAction.getRepeat().hasDuration()) {
                        setAttribute(timerElement, REPEAT_ATTR, escalationAction.getRepeat().getDuration());
                    }
                    writeDelegation(document, timerElement, ACTION_NODE, escalationAction);
                }
                stateElement.appendChild(timerElement);
                // }
            }
            writeTransitions(document, stateElement, state);
        }

        List<WaitState> waitStates = definition.getChildren(WaitState.class);
        for (WaitState waitState : waitStates) {
            Element stateElement = writeWaitState(document, root, waitState);
            writeTransitions(document, stateElement, waitState);
        }

        List<MailNode> mailNodes = definition.getChildren(MailNode.class);
        for (MailNode mailNode : mailNodes) {
            Element nodeElement = writeNode(document, root, mailNode, null);
            setAttribute(nodeElement, "to", mailNode.getRecipient());
            writeTransitions(document, nodeElement, mailNode);
            Element subject = document.createElement("subject");
            setNodeValue(subject, mailNode.getSubject());
            nodeElement.appendChild(subject);
            Element body = document.createElement("body");
            setNodeValue(body, mailNode.getMailBody());
            nodeElement.appendChild(body);
        }

        List<Fork> forks = definition.getChildren(Fork.class);
        for (ru.runa.bpm.ui.common.model.Node node : forks) {
            writeNode(document, root, node, null);
        }

        List<Join> joins = definition.getChildren(Join.class);
        for (ru.runa.bpm.ui.common.model.Node node : joins) {
            writeNode(document, root, node, null);
        }

        List<Subprocess> subprocesses = definition.getChildren(Subprocess.class);
        boolean addSubprocessPermissionHandler = false;
        for (Subprocess subprocess : subprocesses) {
            addSubprocessPermissionHandler = true;
            Element processStateElement = writeNode(document, root, subprocess, null);
            Element subProcessElement = document.createElement(SUB_PROCESS_NODE);
            setAttribute(subProcessElement, NAME_ATTR, subprocess.getSubProcessName());
            setAttribute(subProcessElement, "binding", "late");
            processStateElement.appendChild(subProcessElement);
            for (VariableMapping variable : subprocess.getVariablesList()) {
                Element variableElement = document.createElement(VARIABLE_NODE);
                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
                processStateElement.appendChild(variableElement);
            }
        }

        if (addSubprocessPermissionHandler) {
            // TODO add XML comment and move to own method (after event support will be added)
            ActionImpl action = new ActionImpl();
            action.setDelegationClassName("ru.runa.wf.jbpm.delegation.action.SetSubProcessPermissionsActionHandler");
            writeEvent(document, root, new Event(Event.SUBPROCESS_CREATED), action);
        }

        List<SendMessageNode> sendMessageNodes = definition.getChildren(SendMessageNode.class);
        for (SendMessageNode messageNode : sendMessageNodes) {
            Element messageElement = writeNode(document, root, messageNode, null);
            for (VariableMapping variable : messageNode.getVariablesList()) {
                Element variableElement = document.createElement(VARIABLE_NODE);
                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
                messageElement.appendChild(variableElement);
            }
        }

        List<ReceiveMessageNode> receiveMessageNodes = definition.getChildren(ReceiveMessageNode.class);
        for (ReceiveMessageNode messageNode : receiveMessageNodes) {
            Element messageElement = writeNode(document, root, messageNode, null);
            for (VariableMapping variable : messageNode.getVariablesList()) {
                Element variableElement = document.createElement(VARIABLE_NODE);
                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
                messageElement.appendChild(variableElement);
            }
            if (messageNode.timerExist()) {
                Element timerElement = document.createElement(TIMER_NODE);
                setAttribute(timerElement, DUEDATE_ATTR, messageNode.getDuration().getDuration());
                if (messageNode.getTimerAction() != null) {
                    if (messageNode.getTimerAction().getRepeat().hasDuration()) {
                        setAttribute(timerElement, REPEAT_ATTR, messageNode.getTimerAction().getRepeat().getDuration());
                    }
                    writeDelegation(document, timerElement, ACTION_NODE, messageNode.getTimerAction());
                } else {
                    setAttribute(timerElement, TRANSITION_NODE, PluginConstants.TIMER_TRANSITION_NAME);
                }
                messageElement.appendChild(timerElement);
            }
        }

        EndState endState = definition.getFirstChild(EndState.class);
        if (endState != null) {
            writeElement(document, root, endState);
        }

        for (ElementSerializer elementSerializer : JpdlVersionRegistry.getElementSerializers(jpdlVersion)) {
            elementSerializer.writeToXml(document, definition);
        }

    }

    private Element writeNode(Document document, Element parent, ru.runa.bpm.ui.common.model.Node node, String delegationNodeName) {
        Element nodeElement = writeElement(document, parent, node);
        if (delegationNodeName != null) {
            writeDelegation(document, nodeElement, delegationNodeName, (Delegable) node);
        }
        writeTransitions(document, nodeElement, node);
        return nodeElement;
    }

    private Element writeTaskStateWithDuedate(Document document, Element parent, TaskState state) {
        Element nodeElement = writeElement(document, parent, state);
        Element taskElement = document.createElement(TASK_NODE);
        setAttribute(taskElement, DUEDATE_ATTR, state.getTimeOutDueDate());
        setAttribute(taskElement, NAME_ATTR, state.getName());
        setAttribute(taskElement, SWIMLANE_NODE, state.getSwimlaneName());
        if (state instanceof State && ((State) state).isReassignmentEnabled()) {
            setAttribute(taskElement, REASSIGN_ATTR, "true");
        }
        for (Action action : state.getActions()) {
            ActionImpl actionImpl = (ActionImpl) action;
            writeEvent(document, taskElement, new Event(actionImpl.getEventType()), actionImpl);
        }
        nodeElement.appendChild(taskElement);
        if (state instanceof ITimed && ((ITimed) state).timerExist()) {
            setAttribute(nodeElement, END_TASKS_ATTR, "true");
        }
        return nodeElement;
    }

    private Element writeTaskState(Document document, Element parent, SwimlanedNode state) {
        Element nodeElement = writeElement(document, parent, state);
        Element taskElement = document.createElement(TASK_NODE);
        setAttribute(taskElement, NAME_ATTR, state.getName());
        setAttribute(taskElement, SWIMLANE_NODE, state.getSwimlaneName());
        if (state instanceof State && ((State) state).isReassignmentEnabled()) {
            setAttribute(taskElement, REASSIGN_ATTR, "true");
        }
        for (Action action : state.getActions()) {
            ActionImpl actionImpl = (ActionImpl) action;
            writeEvent(document, taskElement, new Event(actionImpl.getEventType()), actionImpl);
        }
        nodeElement.appendChild(taskElement);
        if (state instanceof ITimed && ((ITimed) state).timerExist()) {
            setAttribute(nodeElement, END_TASKS_ATTR, "true");
        }
        return nodeElement;
    }

    private Element writeWaitState(Document document, Element parent, WaitState state) {
        Element nodeElement = writeElement(document, parent, state, "task-node");
        Element taskElement = document.createElement(TASK_NODE);
        setAttribute(taskElement, NAME_ATTR, state.getName());
        nodeElement.appendChild(taskElement);
        setAttribute(nodeElement, END_TASKS_ATTR, "true");

        Element timerElement = document.createElement(TIMER_NODE);
        setAttribute(timerElement, DUEDATE_ATTR, state.getDueDate());
        if (state.getTimerAction() != null) {
            if (state.getTimerAction().getRepeat().hasDuration()) {
                setAttribute(timerElement, REPEAT_ATTR, state.getTimerAction().getRepeat().getDuration());
            }
            writeDelegation(document, timerElement, ACTION_NODE, state.getTimerAction());
        }
        setAttribute(timerElement, TRANSITION_NODE, PluginConstants.TIMER_TRANSITION_NAME);
        nodeElement.appendChild(timerElement);

        return nodeElement;
    }

    private Element writeElement(Document document, Element parent, GraphElement element) {
        return writeElement(document, parent, element, element.getTypeName());
    }

    private Element writeElement(Document document, Element parent, GraphElement element, String typeName) {
        Element result = document.createElement(typeName);
        if (element instanceof NamedGraphElement) {
            setAttribute(result, NAME_ATTR, ((NamedGraphElement) element).getName());
        }
        if (element instanceof ActionNode) {
            List<Action> nodeActions = ((ActionNode) element).getNodeActions();
            for (Action nodeAction : nodeActions) {
                writeDelegation(document, result, ACTION_NODE, nodeAction);
            }
        }
        if (element instanceof Describable) {
            String description = ((Describable) element).getDescription();
            if (description != null && description.length() > 0) {
                Element desc = document.createElement(DESCRIPTION_NODE);
                setNodeValue(desc, description);
                result.appendChild(desc);
            }
        }
        if (parent != null) {
            parent.appendChild(result);
        }
        return result;
    }

    private void writeTransitions(Document document, Element parent, ru.runa.bpm.ui.common.model.Node node) {
        List<Transition> transitions = node.getLeavingTransitions();
        for (Transition transition : transitions) {
            Element transitionElement = writeElement(document, parent, transition);
            transitionElement.setAttribute(TO_ATTR, transition.getTargetName());
            for (Action action : transition.getActions()) {
                writeDelegation(document, transitionElement, ACTION_NODE, action);
            }
        }
    }

    private void writeEvent(Document document, Element parent, Event event, ActionImpl action) {
        Element eventElement = writeElement(document, parent, event, EVENT_NODE);
        setAttribute(eventElement, "type", event.getType());
        writeDelegation(document, eventElement, ACTION_NODE, action);
    }

    private void writeDelegation(Document document, Element parent, String elementName, Delegable delegable) {
        Element delegationElement = document.createElement(elementName);
        setAttribute(delegationElement, CLASS_ATTR, delegable.getDelegationClassName());
        setAttribute(delegationElement, "config-type", "configuration-property");
        setNodeValue(delegationElement, delegable.getDelegationConfiguration());
        parent.appendChild(delegationElement);
    }
}

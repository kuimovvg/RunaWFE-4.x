package ru.runa.bpm.ui.jpdl2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import ru.runa.bpm.ui.JpdlSerializer;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.Active;
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
import ru.runa.bpm.ui.common.model.StartState;
import ru.runa.bpm.ui.common.model.Subprocess;
import ru.runa.bpm.ui.common.model.Swimlane;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.common.model.WaitState;
import ru.runa.bpm.ui.jpdl2.model.ActionImpl;
import ru.runa.bpm.ui.jpdl2.model.TimerState;
import ru.runa.bpm.ui.util.VariableMapping;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JpdlSerializer_2 extends JpdlSerializer {

    private static final String ACCESS_ATTR = "access";

    private static final String END_STATE_NODE = "end-state";

    private static final String VARIABLE_NODE = "variable";

    private static final String SUB_PROCESS_NODE = "sub-process";

    private static final String MAPPED_NAME_ATTR = "mapped-name";

    private static final String PROCESS_STATE_NODE = "process-state";

    private static final String DECISION_NODE = "decision";

    private static final String JOIN_NODE = "join";

    private static final String FORK_NODE = "fork";

    private static final String DUEDATE_ATTR = "duedate";

    private static final String TIMER_NODE = "timer";

    private static final String ASSIGNMENT_NODE = "assignment";

    private static final String STATE_NODE = "state";

    private static final String START_STATE_NODE = "start-state";

    private static final String SWIMLANE_NODE = "swimlane";

    private static final String EVENT_TYPE_ATTR = "event-type";

    private static final String TO_ATTR = "to";

    private static final String CLASS_ATTR = "class";

    private static final String ACTION_NODE = "action";

    private static final String TRANSITION_NODE = "transition";

    private static final String DELEGATION_NODE = "delegation";

    private static final String DESCRIPTION_NODE = "description";

    private static final String NAME_ATTR = "name";

    private static final String DOCTYPE_SYSTEM_ID = "processdefinition-2.0.dtd";

    @Override
    public boolean isSupported(Document document) {
        DocumentType documentType = document.getDoctype();
        if (documentType == null || documentType.getSystemId() == null) {
            return false;
        }
        return documentType.getSystemId().contains(DOCTYPE_SYSTEM_ID);
    }

    @Override
    public void validateProcessDefinitionXML(IFile file) throws SAXException {
        try {
            XmlUtil.parseDocumentValidateDTD(file.getContents());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Document getInitialProcessDefinitionDocument(String processName) throws ParserConfigurationException {
        Document document = XmlUtil.createDocument("process-definition", null);
        setAttribute(document.getDocumentElement(), NAME_ATTR, processName);
        DocumentType documentType = document.getImplementation().createDocumentType("process-definition", "-//jBpm/jBpm Mapping DTD 2.0//EN",
                DOCTYPE_SYSTEM_ID);
        document.appendChild(documentType);
        return document;
    }

    private <T extends GraphElement> T create(Node node, GraphElement parent) {
        return create(node, parent, node.getNodeName());
    }

    @SuppressWarnings("unchecked")
    private <T extends GraphElement> T create(Node node, GraphElement parent, String nodeType) {
        GraphElement element = JpdlVersionRegistry.getElementTypeDefinition(jpdlVersion, nodeType).createElement();
        if (parent != null) {
            parent.addChild(element);
        }
        if (element instanceof NamedGraphElement) {
            ((NamedGraphElement) element).setName(getAttribute(node, NAME_ATTR));
        }
        NodeList nodeList = node.getChildNodes();
        for (int j = 0; j < nodeList.getLength(); j++) {
            Node childNode = nodeList.item(j);
            if (DESCRIPTION_NODE.equals(childNode.getNodeName()) && element instanceof Describable) {
                ((Describable) element).setDescription(getTextContent(childNode));
            }
            if (DELEGATION_NODE.equals(childNode.getNodeName())) {
                ((Delegable) element).setDelegationClassName(getAttribute(childNode, CLASS_ATTR));
                element.setDelegationConfiguration(getTextContent(childNode));
            }
            if (TRANSITION_NODE.equals(childNode.getNodeName())) {
                parseTransition(childNode, element);
            }
            if (ACTION_NODE.equals(childNode.getNodeName())) {
                parseAction(childNode, element);
            }
        }
        return (T) element;
    }

    private void parseTransition(Node node, GraphElement parent) {
        Transition transition = create(node, parent);
        String targetName = getAttribute(node, TO_ATTR);
        TRANSION_TARGETS.put(transition, targetName);
    }

    private void parseAction(Node node, GraphElement parent) {
        ActionImpl action = create(node, parent);
        action.setEventType(getAttribute(node, EVENT_TYPE_ATTR));
    }

    private static Map<Transition, String> TRANSION_TARGETS = new HashMap<Transition, String>();

    @Override
    public ProcessDefinition parseXML(Document document) {
        TRANSION_TARGETS.clear();
        ProcessDefinition definition = create(document.getDocumentElement(), null);

        NodeList swimlanes = document.getElementsByTagName(SWIMLANE_NODE);
        for (int i = 0; i < swimlanes.getLength(); i++) {
            Node node = swimlanes.item(i);
            create(node, definition);
        }

        NodeList startStates = document.getElementsByTagName(START_STATE_NODE);
        if (startStates.getLength() == 1) {
            Node node = startStates.item(0);
            StartState startState = create(node, definition);
            String swimlaneName = getAttribute(node, SWIMLANE_NODE);
            Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
            startState.setSwimlane(swimlane);
        }

        NodeList states = document.getElementsByTagName(STATE_NODE);
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
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node childNode = nodeList.item(j);
                if (ASSIGNMENT_NODE.equals(childNode.getNodeName()) && state instanceof TimerState) {
                    String swimlaneName = getAttribute(childNode, SWIMLANE_NODE);
                    Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                    ((TimerState) state).setSwimlane(swimlane);
                    String assignmentType = getAttribute(childNode, ASSIGNMENT_NODE);
                    ((TimerState) state).setReassignmentEnabled(assignmentType != null && "reassign".equals(assignmentType));
                }
                if (TIMER_NODE.equals(childNode.getNodeName())) {
                    ((ITimed) state).setDueDate(getAttribute(childNode, DUEDATE_ATTR));
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
        NodeList endStates = document.getElementsByTagName(END_STATE_NODE);
        for (int i = 0; i < endStates.getLength(); i++) {
            Node node = endStates.item(i);
            create(node, definition);
        }

        List<Transition> tmpTransitions = new ArrayList<Transition>(TRANSION_TARGETS.keySet());
        for (Transition transition : tmpTransitions) {
            String targetName = TRANSION_TARGETS.remove(transition);
            ru.runa.bpm.ui.common.model.Node target = definition.getNodeByNameNotNull(targetName);
            transition.setTarget(target);
        }
        return definition;
    }

    @Override
    public void saveToXML(ProcessDefinition definition, Document document) {
        document.removeChild(document.getDocumentElement());

        Element root = writeElement(document, null, definition);
        document.appendChild(root);

        List<Swimlane> swimlanes = definition.getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            Element swimlaneElement = writeElement(document, root, swimlane);
            writeDelegation(document, swimlaneElement, swimlane);
        }

        StartState startState = definition.getFirstChild(StartState.class);
        if (startState != null) {
            Element startStateElement = writeElement(document, root, startState);
            setAttribute(startStateElement, SWIMLANE_NODE, startState.getSwimlaneName());
            writeTransitions(document, startStateElement, startState);
        }

        List<Subprocess> subprocesses = definition.getChildren(Subprocess.class);
        for (ru.runa.bpm.ui.common.model.Node node : subprocesses) {
            writeNode(document, root, node);
        }
        List<Fork> forks = definition.getChildren(Fork.class);
        for (ru.runa.bpm.ui.common.model.Node node : forks) {
            writeNode(document, root, node);
        }
        List<Join> joins = definition.getChildren(Join.class);
        for (ru.runa.bpm.ui.common.model.Node node : joins) {
            writeNode(document, root, node);
        }
        List<Decision> decisions = definition.getChildren(Decision.class);
        for (ru.runa.bpm.ui.common.model.Node node : decisions) {
            writeNode(document, root, node);
        }

        List<TimerState> states = definition.getChildren(TimerState.class);
        for (TimerState state : states) {
            writeState(document, root, state);
        }

        List<WaitState> waitStates = definition.getChildren(WaitState.class);
        for (WaitState state : waitStates) {
            Element waitStateElement = writeElement(document, root, state, "state");
            Element timerElement = document.createElement(TIMER_NODE);
            setAttribute(timerElement, DUEDATE_ATTR, state.getDueDate());
            setAttribute(timerElement, TRANSITION_NODE, PluginConstants.TIMER_TRANSITION_NAME);
            waitStateElement.appendChild(timerElement);
            // writeActions(document, waitStateElement, (Active) state);
            writeTransitions(document, waitStateElement, state);
        }

        EndState endState = definition.getFirstChild(EndState.class);
        if (endState != null) {
            writeElement(document, root, endState);
        }
    }

    private Element writeState(Document document, Element parent, TimerState state) {
        Element stateElement = writeElement(document, parent, state);

        if (state.getSwimlaneName() != null) {
            Element assignmentElement = document.createElement(ASSIGNMENT_NODE);
            setAttribute(assignmentElement, ASSIGNMENT_NODE, state.isReassignmentEnabled() ? "reassign" : "required");
            setAttribute(assignmentElement, SWIMLANE_NODE, state.getSwimlaneName());
            stateElement.appendChild(assignmentElement);
        }
        if (state.timerExist()) {
            Element timerElement = document.createElement(TIMER_NODE);
            setAttribute(timerElement, DUEDATE_ATTR, state.getDuration().getDuration());
            setAttribute(timerElement, TRANSITION_NODE, PluginConstants.TIMER_TRANSITION_NAME);
            stateElement.appendChild(timerElement);
        }

        writeActions(document, stateElement, state);
        writeTransitions(document, stateElement, state);
        return stateElement;
    }

    private Element writeNode(Document document, Element parent, ru.runa.bpm.ui.common.model.Node node) {
        Element nodeElement = writeElement(document, parent, node);
        if (node instanceof Decision) {
            writeDelegation(document, nodeElement, (Decision) node);
        }
        if (node instanceof Active) {
            writeActions(document, nodeElement, (Active) node);
        }
        if (node instanceof Subprocess) {
            Subprocess subprocess = (Subprocess) node;
            Element subProcessElement = document.createElement(SUB_PROCESS_NODE);
            setAttribute(subProcessElement, NAME_ATTR, subprocess.getSubProcessName());
            nodeElement.appendChild(subProcessElement);
            for (VariableMapping variable : subprocess.getVariablesList()) {
                Element variableElement = document.createElement(VARIABLE_NODE);
                setAttribute(variableElement, NAME_ATTR, variable.getProcessVariable());
                setAttribute(variableElement, MAPPED_NAME_ATTR, variable.getSubprocessVariable());
                setAttribute(variableElement, ACCESS_ATTR, variable.getUsage());
                nodeElement.appendChild(variableElement);
            }
        }
        writeTransitions(document, nodeElement, node);
        return nodeElement;
    }

    private Element writeElement(Document document, Element parent, GraphElement element) {
        return writeElement(document, parent, element, element.getTypeName());
    }

    private Element writeElement(Document document, Element parent, GraphElement element, String nodeName) {
        Element result = document.createElement(nodeName);
        if (element instanceof NamedGraphElement) {
            setAttribute(result, NAME_ATTR, ((NamedGraphElement) element).getName());
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
            writeActions(document, transitionElement, transition);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeActions(Document document, Element parent, Active active) {
        List<ActionImpl> actions = (List<ActionImpl>) active.getActions();
        for (ActionImpl action : actions) {
            Element actionElement = writeElement(document, parent, action);
            setAttribute(actionElement, EVENT_TYPE_ATTR, action.getEventType());
            writeDelegation(document, actionElement, action);
        }
    }

    private void writeDelegation(Document document, Element parent, Delegable delegable) {
        Element delegationElement = document.createElement(DELEGATION_NODE);
        setAttribute(delegationElement, CLASS_ATTR, delegable.getDelegationClassName());
        setNodeValue(delegationElement, delegable.getDelegationConfiguration());
        parent.appendChild(delegationElement);
    }
}

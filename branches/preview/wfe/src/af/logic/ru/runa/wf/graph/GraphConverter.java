/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.graph;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.xml.sax.SAXException;

import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.GraphElement;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.Node.NodeType;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.node.InteractionNode;
import ru.runa.bpm.graph.node.MultiInstanceState;
import ru.runa.bpm.graph.node.ProcessState;
import ru.runa.bpm.graph.node.TaskNode;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.par.FileDataProvider;
import ru.runa.bpm.scheduler.def.CancelTimerAction;
import ru.runa.bpm.scheduler.def.CreateTimerAction;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.commons.JBPMLazyLoaderHelper;
import ru.runa.commons.hibernate.HibernateSessionFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.graph.GraphImage.RenderHits;
import ru.runa.wf.graph.figure.AbstractFigure;
import ru.runa.wf.graph.figure.FigureFactory;
import ru.runa.wf.graph.figure.TransitionFigure;
import ru.runa.wf.graph.model.DiagramModel;
import ru.runa.wf.graph.model.NodeModel;
import ru.runa.wf.graph.model.TransitionModel;
import ru.runa.wf.graph.util.DrawProperties;

import com.google.common.base.Objects;

/**
 * Modified on 26.02.2009 by gavrusev_sergei
 */
public class GraphConverter {
    private final ExecutableProcessDefinition processDefinition;
    private final DiagramModel diagramModel;
    private List<Transition> passedTransitions;
    private List<Action> failedActions;
    private Token selectedToken;

    public GraphConverter(ExecutableProcessDefinition processDefinition) throws IOException, SAXException {
        this.processDefinition = processDefinition;
        byte[] gpdBytes = processDefinition.getFileBytesNotNull(FileDataProvider.GPD_XML_FILE_NAME);
        diagramModel = DiagramModel.load(gpdBytes);
    }

    public void setPassedTransitions(List<Transition> passedTransitions) {
        this.passedTransitions = passedTransitions;
    }

    public void setActiveToken(Token activeToken) {
        selectedToken = activeToken;
    }

    public void setFailedActions(List<Action> failedActions) {
        this.failedActions = failedActions;
    }

    public byte[] createDiagram(byte[] graphBytes, ProcessInstance processInstance, Long childProcessId) throws IOException {
        FigureFactory factory = new FigureFactory(!diagramModel.isUmlNotation());
        Map<String, NodeModel> allNodes = new HashMap<String, NodeModel>();
        Map<String, AbstractFigure> allNodeFigures = new HashMap<String, AbstractFigure>();
        // Create all nodes
        for (Node node : processDefinition.getNodes()) {
            NodeModel nodeModel = diagramModel.getNode(node.getName());
            if (diagramModel.isShowActions()) {
                nodeModel.setActionsCount(getNodeActionsCount(node));
            }
            setTypeToNode(node, nodeModel);
            setSwimlaneToNode(node, nodeModel);
            allNodes.put(nodeModel.getName(), nodeModel);
            AbstractFigure nodeFigure = factory.createFigure(nodeModel);
            allNodeFigures.put(nodeModel.getName(), nodeFigure);
        }
        Map<TransitionFigure, RenderHits> transitionFigures = new HashMap<TransitionFigure, RenderHits>();
        Map<AbstractFigure, RenderHits> nodeFigures = new HashMap<AbstractFigure, RenderHits>();
        // Create all transitions and add all nodes
        // TODO only for !DrawProperties.useEdgingOnly()
        for (Node node : processDefinition.getNodes()) {
            NodeModel nodeModel = allNodes.get(node.getName());
            AbstractFigure nodeFigure = allNodeFigures.get(node.getName());
            if (!DrawProperties.useEdgingOnly()) {
                nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));
            }
            int leavingTransitionsCount = node.getLeavingTransitions().size();
            if (node.getLeavingTransition(DrawProperties.TIMEOUT_TRANSITION) != null) {
                leavingTransitionsCount--;
            }
            for (Transition transition : node.getLeavingTransitions()) {
                TransitionModel transitionModel = nodeModel.getTransition(transition.getName());
                if (diagramModel.isShowActions()) {
                    transitionModel.setActionsCount(getTransitionActionsCount(transition));
                }
                AbstractFigure figureTo = allNodeFigures.get(transition.getTo().getName());
                TransitionFigure transitionFigure = factory.createTransitionFigure(transitionModel, nodeFigure, figureTo);
                if (!diagramModel.isUmlNotation()) {
                    boolean exclusiveNode = (nodeFigure.getType() != NodeModel.FORK_JOIN);
                    transitionFigure.setExclusive(exclusiveNode && leavingTransitionsCount > 1);
                }
                if (DrawProperties.TIMEOUT_TRANSITION.equals(transitionModel.getName())) {
                    transitionFigure.setTimerInfo(getTimerInfo(node));
                }
                nodeFigure.addTransition(transition.getName(), transitionFigure);
                if (!DrawProperties.useEdgingOnly()) {
                    transitionFigures.put(transitionFigure, new RenderHits(DrawProperties.getTransitionColor()));
                }
            }
        }
        for (Transition transition : passedTransitions) {
            // Mark 'from' block as PASSED
            AbstractFigure nodeModelFrom = allNodeFigures.get(transition.getFrom().getName());
            nodeFigures.put(nodeModelFrom, new RenderHits(DrawProperties.getHighlightColor(), true));
            // Mark 'to' block as PASSED
            AbstractFigure nodeModelTo = allNodeFigures.get(transition.getTo().getName());
            nodeFigures.put(nodeModelTo, new RenderHits(DrawProperties.getHighlightColor(), true));
            // Mark transition as PASSED
            TransitionFigure transitionFigure = nodeModelFrom.getTransition(transition.getName());
            transitionFigures.put(transitionFigure, new RenderHits(DrawProperties.getHighlightColor(), true));
        }
        fillActiveTasks(processInstance, processInstance.getRootToken(), allNodeFigures, nodeFigures, childProcessId);
        fillExpiredTasks(processInstance, processInstance.getRootToken(), allNodeFigures, nodeFigures, childProcessId);
        for (Action action : failedActions) {
            GraphElement ge = action.getEvent().getGraphElement();
            int index = action.getEvent().getActions().indexOf(action);
            if (ge instanceof Transition) {
                for (TransitionFigure figure : transitionFigures.keySet()) {
                    if (ge.getParent().getName().equals(figure.getFigureFrom().getName())) {
                        figure.addFailedAction(index);
                        break;
                    }
                }
            } else {
                for (AbstractFigure figure : nodeFigures.keySet()) {
                    if (ge.getName().equals(figure.getName())) {
                        figure.addFailedAction(index);
                        break;
                    }
                }
            }
        }
        GraphImage graphImage = new GraphImage(graphBytes, diagramModel, transitionFigures, nodeFigures);
        return graphImage.getImageBytes();
    }

    public byte[] createHistoryDiagram(byte[] graphBytes, ProcessInstance processInstance, List<ProcessLog> logs) throws IOException {
        GraphHistoryBuilder graphHistoryBuilder = new GraphHistoryBuilder(processDefinition.getNodes(), logs, diagramModel);
        graphHistoryBuilder.processLog();
        diagramModel.setHeight(graphHistoryBuilder.getGlYLayer() + 300);
        diagramModel.setWidth(graphHistoryBuilder.getGraphWidth() + 50);
        DrawProperties.setUseEdgingMode(false);
        GraphImage graphImage = new GraphImage(graphBytes, diagramModel, graphHistoryBuilder.getTransitionFigures(),
                graphHistoryBuilder.getNodeFigures());
        byte[] graphImageByteArray = graphImage.getImageBytes();
        DrawProperties.setUseEdgingMode(true);
        return graphImageByteArray;
    }

    public List<GraphElementPresentation> getProcessInstanceUIHistoryData(Subject subject, byte[] graphBytes, ProcessInstance processInstance,
            List<Token> processTokens, List<ProcessLog> logs) throws IOException {
        GraphHistoryBuilder graphHistoryBuilder = new GraphHistoryBuilder(processDefinition.getNodes(), logs, diagramModel);
        graphHistoryBuilder.processLog();
        List<GraphElementPresentation> returnList = graphHistoryBuilder.getLogElements();
        setStartedSubprocesses(subject, returnList, processInstance.getId());
        return returnList;
    }

    private void setStartedSubprocesses(Subject subject, List<GraphElementPresentation> elements, Long processId) {
        Session session = HibernateSessionFactory.openSession();
        List<StartedSubprocesses> subProcesses = session.createQuery(
                "select s from ru.runa.bpm.graph.exe.StartedSubprocesses as s where s.processInstance.id = " + processId).list();
        StartedSubprocessesVisitor startedSubprocessesAddOperation = new StartedSubprocessesVisitor(subject, subProcesses);
        for (GraphElementPresentation elementPresentation : elements) {
            elementPresentation.visit(startedSubprocessesAddOperation);
        }
    }

    private void fillActiveTasks(ProcessInstance processInstance, Token token, Map<String, AbstractFigure> allNodeFigures,
            Map<AbstractFigure, RenderHits> nodeFigures, Long childProcessId) {
        if (token.getEndDate() != null) {
            return;
        }
        for (Token child : token.getChildren().values()) {
            fillActiveTasks(processInstance, child, allNodeFigures, nodeFigures, childProcessId);
        }
        Collection<TaskInstance> unfinishedTasks = processInstance.getTaskMgmtInstance().getUnfinishedTasks(token);
        if (unfinishedTasks.isEmpty() && JBPMLazyLoaderHelper.getImplementation(token.getNode()) instanceof ProcessState) {
            AbstractFigure node = allNodeFigures.get(token.getNode().getName());
            if (unfinishedTasks.isEmpty()
                    && ((selectedToken != null && Objects.equal(selectedToken.getId(), token.getId()) || (token.getSubProcessInstance() != null && Objects
                            .equal(token.getSubProcessInstance().getId(), childProcessId))))) {
                nodeFigures.put(node, new RenderHits(DrawProperties.getHighlightColor(), true, true));
            } else {
                nodeFigures.put(node, new RenderHits(DrawProperties.getBaseColor(), true, true));
            }
        }
        for (TaskInstance task : unfinishedTasks) {
            AbstractFigure node = allNodeFigures.get(task.getName());
            Color color = DrawProperties.getBaseColor();
            if (selectedToken != null && token.getId() == selectedToken.getId()) {
                color = DrawProperties.getHighlightColor();
            }
            Date deadline = task.getDueDate();
            Date warningline = TaskStub.calculateAlmostDeadlineDate(task.getCreateDate(), deadline);
            if (deadline != null && deadline.getTime() < System.currentTimeMillis()) {
                color = DrawProperties.getAlarmColor();
            } else if (warningline != null && warningline.getTime() < System.currentTimeMillis()) {
                color = DrawProperties.getLightAlarmColor();
            }
            nodeFigures.put(node, new RenderHits(color, true, true));
        }
    }

    private void fillExpiredTasks(ProcessInstance processInstance, Token token, Map<String, AbstractFigure> allNodeFigures,
            Map<AbstractFigure, RenderHits> nodeFigures, Long childProcessId) {
        for (Token child : token.getChildren().values()) {
            fillExpiredTasks(processInstance, child, allNodeFigures, nodeFigures, childProcessId);
        }
        Collection<TaskInstance> taskInstances = processInstance.getTaskMgmtInstance().getTaskInstances();
        Iterator<TaskInstance> iter = taskInstances.iterator();
        while (iter.hasNext()) {
            TaskInstance task = iter.next();
            if ((token != null) && (token.equals(task.getToken()))) {
                AbstractFigure figure = allNodeFigures.get(task.getName());
                for (Node node : processDefinition.getNodes()) {
                    if (node.getName().equals(task.getName())) {
                        if (task.getCreateDate() != null && task.getEndDate() != null && task.getDueDate() != null) {
                            Date deadline = task.getDueDate();
                            Date warningline = TaskStub.calculateAlmostDeadlineDate(task.getCreateDate(), deadline);
                            Color color = null;
                            if (deadline != null && deadline.getTime() < task.getEndDate().getTime()) {
                                color = DrawProperties.getAlarmColor();
                            } else if (warningline != null && warningline.getTime() < task.getEndDate().getTime()) {
                                color = DrawProperties.getLightAlarmColor();
                            }
                            if (color != null) {
                                nodeFigures.put(figure, new RenderHits(color, true, true));
                                /*
                                 * CreateTimerAction action =
                                 * getTimerActionIfExists(node); if (action !=
                                 * null) { Date dueDate =
                                 * action.getDueDateDate(new
                                 * ExecutionContext(token), task.getCreate());
                                 * Calendar calEnd = Calendar.getInstance();
                                 * calEnd.setTime(task.getEnd()); Calendar
                                 * calDueDate = Calendar.getInstance();
                                 * calDueDate.setTime(dueDate); long result =
                                 * calEnd.getTimeInMillis() -
                                 * calDueDate.getTimeInMillis(); if (result > 0)
                                 * { Color color =
                                 * DrawProperties.getAlarmColor();
                                 * nodeFigures.put(figure, new RenderHits(color,
                                 * true, true)); } }
                                 */
                            }
                        }/*
                          * else { Date deadline = task.getDueDate();//
                          * getTaskDeadline(task); if (deadline != null &&
                          * deadline.getTime() < System.currentTimeMillis()) {
                          * Color color = DrawProperties.getAlarmColor();
                          * nodeFigures.put(figure, new RenderHits(color, true,
                          * true)); } }
                          */
                    }
                }
            }
        }
    }

    /**
     * Convert nodes to graph elements.
     * 
     * @param definitionNodes
     *            Nodes to convert
     * @return List of graph elements for nodes.
     */
    public List<GraphElementPresentation> getDefinitionElements(List<Node> definitionNodes) {
        List<GraphElementPresentation> result = new ArrayList<GraphElementPresentation>();
        for (Node node : definitionNodes) {
            if (node instanceof HibernateProxy) {
                node = (Node) ((HibernateProxy) node).getHibernateLazyInitializer().getImplementation();
            }
            NodeModel model = diagramModel.getNode(node.getName());
            switch (node.getNodeType()) {
            case SubProcess:
                result.add(new SubprocessGraphElementPresentation(node.getName(), ((ProcessState) node).getSubProcessName(), model.getConstraints()));
                break;
            case MultiInstance:
                result.add(new MultiinstanceGraphElementPresentation(node.getName(), ((MultiInstanceState) node).getSubProcessName(), model
                        .getConstraints()));
                break;
            case Task:
                Task task = ((TaskNode) node).getTasks().iterator().next();
                result.add(new TaskGraphElementPresentation(node.getName(), model.getConstraints(), task.getSwimlane(), model.isMinimizedView()));
                break;
            case State:
                result.add(new StateGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case StartState:
                result.add(new StartStateGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case EndState:
                result.add(new EndStateGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case Fork:
                result.add(new ForkGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case Join:
                result.add(new JoinGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case Decision:
                result.add(new DecisionGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case Node:
                result.add(new NodeGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case SendMessage:
                result.add(new SendMessageGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case ReceiveMessage:
                result.add(new ReceiveMessageGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            default:
                break;
            }
        }
        return result;
    }

    private static int processActionsInEvent(Event nodeEnterEvent) {
        int result = 0;
        if (nodeEnterEvent != null) {
            List<Action> actions = nodeEnterEvent.getActions();
            for (Action action : actions) {
                if (action instanceof HibernateProxy) {
                    action = (Action) ((HibernateProxy) action).getHibernateLazyInitializer().getImplementation();
                }
                if (action instanceof CreateTimerAction || action instanceof CancelTimerAction) {
                    continue;
                }
                result++;
            }
        }
        return result;
    }

    private static int getNodeActionsCount(Node node) {
        int result = 0;
        result += processActionsInEvent(node.getEvent(Event.EVENTTYPE_NODE_ENTER));
        result += processActionsInEvent(node.getEvent(Event.EVENTTYPE_NODE_LEAVE));
        if (node instanceof TaskNode) {
            for (Task task : ((TaskNode) node).getTasks()) {
                result += processActionsInEvent(task.getEvent(Event.EVENTTYPE_TASK_CREATE));
                result += processActionsInEvent(task.getEvent(Event.EVENTTYPE_TASK_ASSIGN));
                result += processActionsInEvent(task.getEvent(Event.EVENTTYPE_TASK_START));
                result += processActionsInEvent(task.getEvent(Event.EVENTTYPE_TASK_END));
            }
        }
        return result;
    }

    private static int getTransitionActionsCount(Transition tr) {
        Event event = tr.getEvent(Event.EVENTTYPE_TRANSITION);
        if (event != null) {
            return event.getActions().size();
        }
        return 0;
    }

    private static void setTypeToNode(Node node, NodeModel nodeModel) {
        if (node.getNodeType() == NodeType.Decision) {
            nodeModel.setType(NodeModel.DECISION);
        } else if (node.getNodeType() == NodeType.Fork || node.getNodeType() == NodeType.Join) {
            nodeModel.setType(NodeModel.FORK_JOIN);
        } else if (node.getNodeType() == NodeType.EndState) {
            nodeModel.setType(NodeModel.END_STATE);
        } else if (node.getNodeType() == NodeType.StartState) {
            nodeModel.setType(NodeModel.START_STATE);
        } else if (node.getNodeType() == NodeType.SubProcess) {
            nodeModel.setType(NodeModel.PROCESS_STATE);
        } else if (node.getNodeType() == NodeType.MultiInstance) {
            nodeModel.setType(NodeModel.MULTI_INSTANCE);
        } else if (node.getNodeType() == NodeType.Node) {
            nodeModel.setType(NodeModel.ACTION_NODE);
        } else if (node.getNodeType() == NodeType.SendMessage) {
            nodeModel.setType(NodeModel.SEND_MESSAGE);
        } else if (node.getNodeType() == NodeType.ReceiveMessage) {
            nodeModel.setType(NodeModel.RECEIVE_MESSAGE);
        } else {
            // TODO CreateTimerAction createTimerAction =
            // getTimerActionIfExists(node);
            boolean hasTimer = (getTimerActionIfExists(node) != null && !"__GLOBAL".equals(getTimerActionIfExists(node).getTimerName()) && !"__LOCAL"
                    .equals(getTimerActionIfExists(node).getTimerName()));
            boolean hasTimeOutTransition = false;
            Collection<Transition> transitions = node.getLeavingTransitions();
            for (Transition tr : transitions) {
                if (DrawProperties.TIMEOUT_TRANSITION.equals(tr.getName())) {
                    hasTimeOutTransition = true;
                }
            }
            if (!hasTimer) {
                nodeModel.setType(NodeModel.STATE);
            } else if (hasTimeOutTransition && transitions.size() == 1) {
                nodeModel.setType(NodeModel.WAIT_STATE);
            } else {
                nodeModel.setType(NodeModel.STATE_WITH_TIMER);
            }
        }
    }

    private static void setSwimlaneToNode(Node node, NodeModel nodeModel) {
        Task task = null;
        if (node instanceof HibernateProxy) {
            node = (Node) ((HibernateProxy) node).getHibernateLazyInitializer().getImplementation();
        }
        if (node instanceof InteractionNode) {
            task = ((InteractionNode) node).getFirstTaskNotNull();
        }
        if (task != null && task.getSwimlane() != null) {
            nodeModel.setSwimlane(task.getSwimlane().getName());
        }
    }

    private static CreateTimerAction getTimerActionIfExists(Node node) {
        Event nodeEnterEvent = node.getEvent(Event.EVENTTYPE_NODE_ENTER);
        if (nodeEnterEvent != null) {
            List<Action> actions = nodeEnterEvent.getActions();
            if (actions.size() > 0) {
                Action action = actions.get(0);
                if (action instanceof HibernateProxy) {
                    action = (Action) ((HibernateProxy) action).getHibernateLazyInitializer().getImplementation();
                }
                if (action instanceof CreateTimerAction) {
                    return (CreateTimerAction) action;
                }
            }
        }
        return null;
    }

    private static String getTimerInfo(Node node) {
        try {
            CreateTimerAction action = getTimerActionIfExists(node);
            if (action == null) {
                return "No timer";
            }
            return action.getDueDate();
        } catch (Exception e) {
            return e.getClass().getName();
        }
    }

}

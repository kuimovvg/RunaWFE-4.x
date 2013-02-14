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
package ru.runa.wfe.graph.image;

import java.awt.Color;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.graph.image.GraphImage.RenderHits;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigureBase;
import ru.runa.wfe.graph.image.figure.bpmn.BPMNFigureFactory;
import ru.runa.wfe.graph.image.figure.uml.UMLFigureFactory;
import ru.runa.wfe.graph.image.model.DiagramModel;
import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.graph.image.model.TransitionModel;
import ru.runa.wfe.graph.image.util.DrawProperties;
import ru.runa.wfe.job.CancelTimerAction;
import ru.runa.wfe.job.CreateTimerAction;
import ru.runa.wfe.job.Timer;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubProcessState;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTaskFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 * Modified on 26.02.2009 by gavrusev_sergei
 */
public class GraphImageBuilder {
    private final WfTaskFactory taskObjectFactory;
    private final ProcessDefinition processDefinition;
    private Token highlightedToken;
    private final Map<String, NodeModel> allNodes = Maps.newHashMap();
    private final Map<String, AbstractFigure> allNodeFigures = Maps.newHashMap();
    private final Map<TransitionFigureBase, RenderHits> transitionFigureBases = Maps.newHashMap();
    private final Map<AbstractFigure, RenderHits> nodeFigures = Maps.newHashMap();

    public GraphImageBuilder(WfTaskFactory taskObjectFactory, ProcessDefinition processDefinition) {
        this.taskObjectFactory = taskObjectFactory;
        this.processDefinition = processDefinition;
    }

    public void setHighlightedToken(Token highlightedToken) {
        this.highlightedToken = highlightedToken;
    }

    public byte[] createDiagram(Process process, List<Transition> passedTransitions) throws Exception {
        DiagramModel diagramModel = DiagramModel.load(processDefinition);
        AbstractFigureFactory factory;
        if (diagramModel.isUmlNotation()) {
            factory = new UMLFigureFactory();
        } else {
            factory = new BPMNFigureFactory(diagramModel.isGraphiti());
        }
        // Create all nodes
        for (Node node : processDefinition.getNodes()) {
            NodeModel nodeModel = diagramModel.getNode(node.getNodeId());
            if (diagramModel.isShowActions()) {
                nodeModel.setActionsCount(getNodeActionsCount(node));
            }
            initNodeModel(node, nodeModel);
            allNodes.put(nodeModel.getNodeId(), nodeModel);
            AbstractFigure nodeFigure = factory.createFigure(nodeModel);
            allNodeFigures.put(nodeModel.getNodeId(), nodeFigure);
        }
        for (Node node : processDefinition.getNodes()) {
            NodeModel nodeModel = allNodes.get(node.getNodeId());
            AbstractFigure nodeFigure = allNodeFigures.get(node.getNodeId());
            if (!DrawProperties.useEdgingOnly()) {
                nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));
            }
            int leavingTransitionsCount = node.getLeavingTransitions().size();
            if (node.hasLeavingTransition(Transition.TIMEOUT_TRANSITION_NAME)) {
                leavingTransitionsCount--;
            }
            for (Transition transition : node.getLeavingTransitions()) {
                TransitionModel transitionModel = nodeModel.getTransition(transition.getName());
                if (diagramModel.isShowActions()) {
                    transitionModel.setActionsCount(getTransitionActionsCount(transition));
                }
                AbstractFigure figureTo = allNodeFigures.get(transition.getTo().getNodeId());
                TransitionFigureBase transitionFigureBase = factory.createTransitionFigure(transitionModel, nodeFigure, figureTo);
                transitionFigureBase.init(transitionModel, nodeFigure, figureTo);
                if (!diagramModel.isUmlNotation()) {
                    boolean exclusiveNode = (nodeModel.getType() != NodeType.Fork && nodeModel.getType() != NodeType.Join);
                    transitionFigureBase.setExclusive(exclusiveNode && leavingTransitionsCount > 1);
                }
                if (Transition.TIMEOUT_TRANSITION_NAME.equals(transitionModel.getName())) {
                    transitionFigureBase.setTimerInfo(getTimerInfo(node));
                }
                nodeFigure.addTransition(transition.getName(), transitionFigureBase);
                if (!DrawProperties.useEdgingOnly()) {
                    transitionFigureBases.put(transitionFigureBase, new RenderHits(DrawProperties.getTransitionColor()));
                }
            }
        }
        for (Transition transition : passedTransitions) {
            // Mark 'from' block as PASSED
            AbstractFigure nodeModelFrom = allNodeFigures.get(transition.getFrom().getNodeId());
            nodeFigures.put(nodeModelFrom, new RenderHits(DrawProperties.getHighlightColor(), true));
            // Mark 'to' block as PASSED
            AbstractFigure nodeModelTo = allNodeFigures.get(transition.getTo().getNodeId());
            nodeFigures.put(nodeModelTo, new RenderHits(DrawProperties.getHighlightColor(), true));
            // Mark transition as PASSED
            TransitionFigureBase transitionFigureBase = nodeModelFrom.getTransition(transition.getName());
            transitionFigureBases.put(transitionFigureBase, new RenderHits(DrawProperties.getHighlightColor(), true));
        }
        fillActiveSubprocesses(process.getRootToken());
        fillActiveTasks(process);
        fillExpiredTasks(process);
        GraphImage graphImage = new GraphImage(processDefinition.getGraphImageBytes(), diagramModel, transitionFigureBases, nodeFigures);
        return graphImage.getImageBytes();
    }

    private void fillActiveSubprocesses(Token token) {
        for (Token childToken : token.getActiveChildren()) {
            fillActiveSubprocesses(childToken);
        }
        if (token.getNode(processDefinition) instanceof SubProcessState) {
            AbstractFigure node = allNodeFigures.get(token.getNode(processDefinition).getNodeId());
            Color color;
            if (highlightedToken != null && Objects.equal(highlightedToken.getId(), token.getId())) {
                color = DrawProperties.getHighlightColor();
            } else {
                color = DrawProperties.getBaseColor();
            }
            nodeFigures.put(node, new RenderHits(color, true, true));
        }
    }

    private void fillActiveTasks(Process process) {
        for (Task task : process.getActiveTasks(null)) {
            AbstractFigure node = allNodeFigures.get(task.getNodeId());
            Color color = DrawProperties.getBaseColor();
            if (highlightedToken != null && Objects.equal(task.getToken().getId(), highlightedToken.getId())) {
                color = DrawProperties.getHighlightColor();
            }
            Date deadlineWarningDate = taskObjectFactory.getDeadlineWarningDate(task);
            if (task.getDeadlineDate() != null && task.getDeadlineDate().getTime() < System.currentTimeMillis()) {
                color = DrawProperties.getAlarmColor();
            } else if (deadlineWarningDate != null && deadlineWarningDate.getTime() < System.currentTimeMillis()) {
                color = DrawProperties.getLightAlarmColor();
            }
            nodeFigures.put(node, new RenderHits(color, true, true));
        }
    }

    private void fillExpiredTasks(Process process) {
        for (Task task : process.getTasks()) {
            AbstractFigure figure = allNodeFigures.get(task.getNodeId());
            for (Node node : processDefinition.getNodes()) {
                if (Objects.equal(node.getNodeId(), task.getNodeId())) {
                    if (task.isActive() || task.getDeadlineDate() == null) {
                        continue;
                    }
                    Date deadlineWarningDate = taskObjectFactory.getDeadlineWarningDate(task);
                    Color color;
                    if (task.getDeadlineDate() != null && task.getDeadlineDate().getTime() < task.getEndDate().getTime()) {
                        color = DrawProperties.getAlarmColor();
                    } else if (deadlineWarningDate != null && deadlineWarningDate.getTime() < task.getEndDate().getTime()) {
                        color = DrawProperties.getLightAlarmColor();
                    } else {
                        continue;
                    }
                    nodeFigures.put(figure, new RenderHits(color, true, true));
                }
            }
        }
    }

    private static int processActionsInEvent(Event event) {
        int result = 0;
        for (Action action : event.getActions()) {
            if (action instanceof CreateTimerAction || action instanceof CancelTimerAction || Timer.ESCALATION_NAME.equals(action.getName())) {
                continue;
            }
            result++;
        }
        return result;
    }

    private static int getNodeActionsCount(GraphElement node) {
        int result = 0;
        for (Event event : node.getEvents().values()) {
            result += processActionsInEvent(event);
        }
        if (node instanceof TaskNode) {
            for (TaskDefinition taskDefinition : ((TaskNode) node).getTasks()) {
                result += getNodeActionsCount(taskDefinition);
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

    private static void initNodeModel(Node node, NodeModel nodeModel) {
        nodeModel.setType(node.getNodeType());
        // nodeModel contains only id
        nodeModel.setName(node.getName());
        CreateTimerAction createTimerAction = getTimerActionIfExists(node);
        boolean hasTimer = (createTimerAction != null && !Timer.ESCALATION_NAME.equals(createTimerAction.getName()));
        nodeModel.setWithTimer(hasTimer);
        if (node instanceof Synchronizable) {
            nodeModel.setAsync(((Synchronizable) node).isAsync());
        }
        TaskDefinition taskDefinition = null;
        if (node instanceof InteractionNode) {
            taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
        }
        if (taskDefinition != null && taskDefinition.getSwimlane() != null) {
            nodeModel.setSwimlane(taskDefinition.getSwimlane().getName());
        }
    }

    private static CreateTimerAction getTimerActionIfExists(Node node) {
        for (Event event : node.getEvents().values()) {
            for (Action action : event.getActions()) {
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

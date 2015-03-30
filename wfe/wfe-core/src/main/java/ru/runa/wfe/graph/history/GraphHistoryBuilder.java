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
package ru.runa.wfe.graph.history;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.NodeLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.TaskLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.history.GraphImage.RenderHits;
import ru.runa.wfe.graph.history.figure.AbstractFigure;
import ru.runa.wfe.graph.history.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.history.figure.TransitionFigureBase;
import ru.runa.wfe.graph.history.figure.uml.UMLFigureFactory;
import ru.runa.wfe.graph.history.model.BendpointModel;
import ru.runa.wfe.graph.history.model.DiagramModel;
import ru.runa.wfe.graph.history.model.NodeModel;
import ru.runa.wfe.graph.history.model.TransitionModel;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;
import ru.runa.wfe.history.graph.HistoryGraphBuilder;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;
import ru.runa.wfe.history.layout.CalculateGraphLayout;
import ru.runa.wfe.history.layout.CalculateGraphLayoutContext;
import ru.runa.wfe.history.layout.CalculateSubTreeBounds;
import ru.runa.wfe.history.layout.NodeLayoutData;
import ru.runa.wfe.history.layout.PushWidthDown;
import ru.runa.wfe.history.layout.TransitionOrderer;
import ru.runa.wfe.history.layout.TransitionOrdererContext;
import ru.runa.wfe.lang.EmbeddedSubprocessStartNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubProcessState;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class GraphHistoryBuilder {
    private final Map<HistoryGraphNode, NodeModel> allNodes = Maps.newHashMap();
    private final Map<HistoryGraphNode, AbstractFigure> allNodeFigures = Maps.newHashMap();
    private final Map<TransitionFigureBase, RenderHits> transitionFigureBases = Maps.newHashMap();
    private final Map<AbstractFigure, RenderHits> nodeFigures = Maps.newHashMap();

    private final DiagramModel diagramModel;
    private final AbstractFigureFactory factory = new UMLFigureFactory();

    private final List<GraphElementPresentation> logElements = new ArrayList<GraphElementPresentation>();

    private final GraphHistoryBuilderData data;

    public GraphHistoryBuilder(List<Executor> executors, Process processInstance, ProcessDefinition processDefinition,
            List<ProcessLog> fullProcessLogs, String subProcessId) {
        this.data = new GraphHistoryBuilderData(executors, processInstance, processDefinition, fullProcessLogs, subProcessId);
        diagramModel = (subProcessId != null && !"null".equals(subProcessId)) ? DiagramModel.load(processDefinition
                .getEmbeddedSubprocessByIdNotNull(subProcessId)) : DiagramModel.load(processDefinition);
    }

    public byte[] createDiagram(Process process, List<Transition> passedTransitions) throws Exception {

        HistoryGraphNode root = HistoryGraphBuilder.buildHistoryGraph(data.getProcessLogs(), data.getProcessDefinitionData());
        root.processBy(new CalculateSubTreeBounds(diagramModel), null);
        root.processBy(new PushWidthDown(), -1);
        root.processBy(new TransitionOrderer(), new TransitionOrdererContext());
        root.processBy(new CalculateGraphLayout(), new CalculateGraphLayoutContext(NodeLayoutData.get(root).getSubtreeHeight()));
        // root.processBy(new JoinParallelWidthTuner(), null);
        HashSet<HistoryGraphNode> visited = new HashSet<HistoryGraphNode>();
        calculateCoordinatesForNodes(root, null, null, visited);

        diagramModel.setHeight(NodeLayoutData.get(root).getSubtreeHeight());
        diagramModel.setWidth(NodeLayoutData.get(root).getSubtreeWidth());
        GraphImage graphImage = new GraphImage(null, diagramModel, transitionFigureBases, nodeFigures, false);
        return graphImage.getImageBytes();
    }

    private NodeModel calculateCoordinatesForNodes(HistoryGraphNode root, Map<String, List<String>> forkNodes, Map<String, Long> nodeRepetitionCount,
            HashSet<HistoryGraphNode> visited) {
        if (visited.contains(root)) {
            return allNodes.get(root);
        }
        visited.add(root);
        if (forkNodes == null) {
            forkNodes = new HashMap<String, List<String>>();
        }
        if (nodeRepetitionCount == null) {
            nodeRepetitionCount = new HashMap<String, Long>();
        }

        String nodeId = root.getNodeId();

        Node node = data.getNode(nodeId);
        NodeModel nodeModelForClone = diagramModel.getNodeNotNull(node.getNodeId());
        NodeModel nodeModel = new NodeModel();
        nodeModel.setNodeId(nodeModelForClone.getNodeId());
        nodeModel.setMinimizedView(nodeModelForClone.isMinimizedView());
        nodeModel.setTimerTransitionName(nodeModelForClone.getTimerTransitionName());
        nodeModel.setAsync(nodeModelForClone.isAsync());
        nodeModel.setSwimlane(nodeModelForClone.getSwimlane());
        nodeModel.setWidth(nodeModelForClone.getWidth());
        nodeModel.setHeight(nodeModelForClone.getHeight());
        for (TransitionModel transitionModelForClone : nodeModelForClone.getTransitions().values()) {
            TransitionModel transitionModel = new TransitionModel();
            transitionModel.setName(transitionModelForClone.getName());
            transitionModel.setActionsCount(transitionModelForClone.getActionsCount());
            transitionModel.setNodeFrom(transitionModelForClone.getNodeFrom());
            transitionModel.setNodeTo(transitionModelForClone.getNodeTo());
            nodeModel.addTransition(transitionModel);
        }

        if (diagramModel.isShowActions()) {
            nodeModel.setActionsCount(GraphImageHelper.getNodeActionsCount(node));
        }
        GraphImageHelper.initNodeModel(node, nodeModel);

        nodeModel.setX(NodeLayoutData.get(root).getX());
        nodeModel.setY(NodeLayoutData.get(root).getY());
        if (NodeLayoutData.get(root).getPreferredWidth() > 0) {
            nodeModel.setWidth(NodeLayoutData.get(root).getPreferredWidth());
        }
        nodeModel.setType(data.getNodeType(nodeId));
        if (nodeModel.getType() == NodeType.FORK || nodeModel.getType() == NodeType.JOIN) {
            nodeModel.setWidth(NodeLayoutData.get(root).getPreferredWidth());
            if (nodeModel.getHeight() > 4) {
                nodeModel.setHeight(4);
            }
        }
        allNodes.put(root, nodeModel);

        AbstractFigure nodeFigure = factory.createFigure(nodeModel, false);
        allNodeFigures.put(root, nodeFigure);
        nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));
        NodeEnterLog nodeEnterLog = root.getNodeEnterLog();
        if (nodeEnterLog != null) {
            addedTooltipOnGraph(node, nodeFigure, nodeModel, nodeEnterLog);
        }

        for (HistoryGraphTransitionModel historyTransition : root.getTransitions()) {
            NodeModel transitionTo = calculateCoordinatesForNodes(historyTransition.getToNode(), forkNodes, nodeRepetitionCount, visited);
            for (Transition transition : node.getLeavingTransitions()) {
                if (!historyTransition.getNodeId().equals(transition.getNodeId())) {
                    continue;
                }

                TransitionModel transitionModel = nodeModel.getTransition(transition.getName());
                transitionModel.getBendpoints().clear();

                TransitionLog transitionLog = historyTransition.getLog();
                if (transitionLog != null) {
                    transitionModel.setName(CalendarUtil.formatDateTime(transitionLog.getCreateDate()));
                }

                if (diagramModel.isShowActions()) {
                    transitionModel.setActionsCount(GraphImageHelper.getTransitionActionsCount(transition));
                }

                String toNodeId = null;
                Node toNode = transition.getTo();
                if (toNode instanceof EmbeddedSubprocessStartNode) {
                    toNodeId = ((EmbeddedSubprocessStartNode) toNode).getTransitionNodeId(true);
                } else {
                    toNodeId = toNode.getNodeId();
                }

                AbstractFigure figureTo = allNodeFigures.get(historyTransition.getToNode());

                if (figureTo != null) {
                    if (data.getNodeType(nodeModel.getNodeId()) == NodeType.FORK) {
                        BendpointModel bendpointModel = new BendpointModel();
                        bendpointModel.setX(figureTo.getCoords()[0] + figureTo.getCoords()[2] / 2);
                        bendpointModel.setY(nodeModel.getY() + nodeModel.getHeight() / 2);
                        transitionModel.addBendpoint(bendpointModel);
                    }
                    if (data.getNodeType(nodeModel.getNodeId()) == NodeType.JOIN) {
                        BendpointModel bendpointModel = new BendpointModel();
                        bendpointModel.setX(nodeModel.getX() + nodeModel.getWidth() / 2);
                        bendpointModel.setY(figureTo.getCoords()[1]);
                        transitionModel.addBendpoint(bendpointModel);
                    }
                    TransitionFigureBase transitionFigureBase = factory.createTransitionFigure(transitionModel, nodeFigure, figureTo);
                    transitionFigureBase.init(transitionModel, nodeFigure, figureTo);
                    if (Objects.equal(nodeModel.getTimerTransitionName(), transitionModel.getName())) {
                        transitionFigureBase.setTimerInfo(GraphImageHelper.getTimerInfo(node));
                    }
                    nodeFigure.addTransition(transition.getName(), transitionFigureBase);
                    transitionFigureBases.put(transitionFigureBase, new RenderHits(DrawProperties.getTransitionColor()));
                }
            }
        }
        return nodeModel;
    }

    private void addedTooltipOnGraph(Node node, AbstractFigure figure, NodeModel nodeModel, NodeEnterLog nodeEnterlog) {
        // find node leave log and taskEnterLog
        NodeLeaveLog nodeLeaveLog = null;
        for (NodeLog nodeLog : data.getNodeLogs()) {
            if (nodeLog.getId() > nodeEnterlog.getId() && nodeLog instanceof NodeLeaveLog
                    && nodeEnterlog.getNodeId().equals(((NodeLeaveLog) nodeLog).getNodeId())) {
                nodeLeaveLog = (NodeLeaveLog) nodeLog;
                break;
            }
        }

        GraphElementPresentation presentation;
        switch (nodeModel.getType()) {
        case SUBPROCESS:
            presentation = new SubprocessGraphElementPresentation();
            ((SubprocessGraphElementPresentation) presentation).setSubprocessAccessible(true);
            for (NodeLog nodeLog : data.getNodeLogs()) {
                if (nodeLog instanceof SubprocessStartLog && nodeEnterlog.getNodeId().equals(((SubprocessStartLog) nodeLog).getNodeId())) {
                    ((SubprocessGraphElementPresentation) presentation).setSubprocessId(((SubprocessStartLog) nodeLog).getSubprocessId());
                    break;
                }

                if (nodeLog instanceof NodeEnterLog && nodeEnterlog.getNodeId().equals(((NodeEnterLog) nodeLog).getNodeId())
                        && ((NodeEnterLog) nodeLog).getNodeType() == NodeType.SUBPROCESS && node instanceof SubProcessState) {
                    //
                    // find first SubprocessStartLog
                    for (NodeLog nodeLog2 : data.getNodeLogs()) {
                        if (nodeLog2.getId() >= nodeLog.getId() && nodeLog2 instanceof SubprocessStartLog) {
                            ((SubprocessGraphElementPresentation) presentation).setSubprocessId(((SubprocessStartLog) nodeLog2).getSubprocessId());
                            break;
                        }
                    }

                    if (((SubProcessState) node).isEmbedded()) {
                        ((SubprocessGraphElementPresentation) presentation).setSubprocessId(((NodeEnterLog) nodeLog).getProcessId());
                        SubprocessDefinition subprocessDefinition = data.getEmbeddedSubprocess(((SubProcessState) node).getSubProcessName());
                        ((SubprocessGraphElementPresentation) presentation).setEmbeddedSubprocessId(subprocessDefinition.getNodeId());
                        ((SubprocessGraphElementPresentation) presentation).setEmbeddedSubprocessGraphWidth(subprocessDefinition
                                .getGraphConstraints()[2]);
                        ((SubprocessGraphElementPresentation) presentation).setEmbeddedSubprocessGraphHeight(subprocessDefinition
                                .getGraphConstraints()[3]);

                        if (nodeLeaveLog == null) {
                            presentation.initialize(node, nodeModel.getConstraints());
                            presentation.setData("");
                            logElements.add(presentation);
                            return;
                        }
                    }

                    break;
                }
            }

            break;
        case MULTI_SUBPROCESS:
            presentation = new MultiinstanceGraphElementPresentation();
            Iterator<ProcessLog> logIterator = data.getProcessLogs().iterator();
            boolean subProcessStartedLog = false;
            while (logIterator.hasNext()) {
                ProcessLog processLog = logIterator.next();
                if (processLog.getId() > nodeEnterlog.getId() && processLog instanceof SubprocessStartLog) {
                    ((MultiinstanceGraphElementPresentation) presentation).addSubprocessInfo(((SubprocessStartLog) processLog).getSubprocessId(),
                            true, false);
                    subProcessStartedLog = true;
                } else if (processLog.getId() > nodeEnterlog.getId() && !(processLog instanceof SubprocessStartLog) && subProcessStartedLog) {
                    subProcessStartedLog = false;
                    break;
                }
            }
            break;
        case TASK_STATE:
            presentation = new TaskGraphElementPresentation();
            break;
        default:
            presentation = new GraphElementPresentation();
        }

        if (nodeLeaveLog == null) {
            return;
        }

        presentation.initialize(node, nodeModel.getConstraints());

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(nodeEnterlog.getCreateDate());
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(nodeLeaveLog.getCreateDate());
        Long period = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(period);
        String date = getPeriodDateString(startCal, endCal);

        if (nodeModel.getType().equals(NodeType.SUBPROCESS) || nodeModel.getType().equals(NodeType.MULTI_SUBPROCESS)) {
            presentation.setData("Time period is " + date);
        } else if (nodeModel.getType().equals(NodeType.TASK_STATE)) {
            StringBuffer str = new StringBuffer();

            TaskCreateLog taskCreateLog = null;
            TaskEndLog taskEndLog = null;
            for (ProcessLog processLog : data.getProcessLogs()) {
                if (processLog.getId() > nodeEnterlog.getId()) {
                    if (processLog instanceof TaskCreateLog) {
                        taskCreateLog = (TaskCreateLog) processLog;
                        continue;
                    } else if (processLog instanceof TaskEndLog && taskCreateLog != null
                            && taskCreateLog.getTaskName().equals(((TaskEndLog) processLog).getTaskName())) {
                        taskEndLog = (TaskEndLog) processLog;
                        break;
                    }
                }
            }

            if (taskEndLog != null) {
                String actor = taskEndLog.getActorName();

                TaskAssignLog prev = null;
                for (TaskLog tempLog : data.getTaskLogs()) {
                    if (tempLog instanceof TaskAssignLog) {
                        prev = (TaskAssignLog) tempLog;
                    } else if (tempLog.equals(taskEndLog)) {
                        break;
                    }
                }

                if (prev != null) {
                    if (prev.getOldExecutorName() != null && !prev.getOldExecutorName().equals(actor)) {
                        actor = prev.getOldExecutorName();
                    }
                }

                Executor performedTaskExecutor = data.getExecutorByName(actor);
                if (performedTaskExecutor != null) {
                    if (performedTaskExecutor instanceof Actor && ((Actor) performedTaskExecutor).getFullName() != null) {
                        str.append("Full Name is " + ((Actor) performedTaskExecutor).getFullName() + ".</br>");
                    }
                    str.append("Login is " + performedTaskExecutor.getName() + ".</br>");
                }
            }

            str.append("Time period is " + date + ".");
            presentation.setData(str.toString());
        }

        logElements.add(presentation);
    }

    private String getPeriodDateString(Calendar startCal, Calendar endCal) {
        long period = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(period);
        periodCal.setTimeInMillis(period - periodCal.getTimeZone().getOffset(period));

        String result = "";
        long days = period / (24 * 60 * 60 * 1000);

        if (days > 0) {
            result = (days == 1) ? "1 day " : (String.valueOf(days) + " days ");
        }

        result = result + CalendarUtil.formatTime(periodCal.getTime());

        return result;
    }

    public List<GraphElementPresentation> getLogElements() {
        return logElements;
    }
}

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

import java.util.List;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.graph.history.figure.CreateGraphFigures;
import ru.runa.wfe.graph.history.figure.CreateGraphFiguresContext;
import ru.runa.wfe.graph.history.model.DiagramModel;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.history.graph.HistoryGraphBuilder;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.layout.CalculateGraphLayout;
import ru.runa.wfe.history.layout.CalculateGraphLayoutContext;
import ru.runa.wfe.history.layout.CalculateSubTreeBounds;
import ru.runa.wfe.history.layout.NodeLayoutData;
import ru.runa.wfe.history.layout.PushWidthDown;
import ru.runa.wfe.history.layout.TransitionOrderer;
import ru.runa.wfe.history.layout.TransitionOrdererContext;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.user.Executor;

public class GraphHistoryBuilder {
    private final DiagramModel diagramModel;

    private final GraphHistoryBuilderData data;

    public GraphHistoryBuilder(List<Executor> executors, Process processInstance, ProcessDefinition processDefinition,
            List<ProcessLog> fullProcessLogs, String subProcessId) {
        this.data = new GraphHistoryBuilderData(executors, processInstance, processDefinition, fullProcessLogs, subProcessId);
        diagramModel = (subProcessId != null && !"null".equals(subProcessId)) ? DiagramModel.load(processDefinition
                .getEmbeddedSubprocessByIdNotNull(subProcessId)) : DiagramModel.load(processDefinition);
    }

    public byte[] createDiagram(Process process) throws Exception {
        HistoryGraphNode root = BuildHistoryGraph();
        CreateHistoryGraphImage createImageOperation = new CreateHistoryGraphImage(diagramModel);
        root.processBy(createImageOperation, new CreateHistoryGraphImageContext());
        return createImageOperation.getImageBytes();
    }

    public List<GraphElementPresentation> getPresentations(Process process) throws Exception {
        HistoryGraphNode root = BuildHistoryGraph();
        CreateGraphElementPresentation createPresentationOperation = new CreateGraphElementPresentation(data);
        root.processBy(createPresentationOperation, new CreateGraphElementPresentationContext());
        return createPresentationOperation.getPresentationElements();
    }

    private HistoryGraphNode BuildHistoryGraph() {
        HistoryGraphNode root = HistoryGraphBuilder.buildHistoryGraph(data.getProcessLogs(), data.getProcessDefinitionData());
        root.processBy(new CalculateSubTreeBounds(diagramModel), null);
        root.processBy(new PushWidthDown(), -1);
        root.processBy(new TransitionOrderer(), new TransitionOrdererContext());
        root.processBy(new CalculateGraphLayout(), new CalculateGraphLayoutContext(NodeLayoutData.get(root).getSubtreeHeight()));
        diagramModel.setHeight(NodeLayoutData.get(root).getSubtreeHeight());
        diagramModel.setWidth(NodeLayoutData.get(root).getSubtreeWidth());
        root.processBy(new CreateGraphFigures(diagramModel), new CreateGraphFiguresContext());
        return root;
    }

    /*
     * private void calculateCoordinatesForNodes(HistoryGraphNode root,
     * HashSet<HistoryGraphNode> visited) { if (visited.contains(root)) {
     * return; } visited.add(root); String nodeId = root.getNodeId();
     * 
     * Node node = data.getNode(nodeId); NodeModel nodeModelForClone =
     * diagramModel.getNodeNotNull(node.getNodeId()); NodeModel nodeModel = new
     * NodeModel(); nodeModel.setNodeId(nodeModelForClone.getNodeId());
     * nodeModel.setMinimizedView(nodeModelForClone.isMinimizedView());
     * nodeModel
     * .setTimerTransitionName(nodeModelForClone.getTimerTransitionName());
     * nodeModel.setAsync(nodeModelForClone.isAsync());
     * nodeModel.setSwimlane(nodeModelForClone.getSwimlane());
     * nodeModel.setWidth(nodeModelForClone.getWidth());
     * nodeModel.setHeight(nodeModelForClone.getHeight()); for (TransitionModel
     * transitionModelForClone : nodeModelForClone.getTransitions().values()) {
     * TransitionModel transitionModel = new TransitionModel();
     * transitionModel.setName(transitionModelForClone.getName());
     * transitionModel
     * .setActionsCount(transitionModelForClone.getActionsCount());
     * transitionModel.setNodeFrom(transitionModelForClone.getNodeFrom());
     * transitionModel.setNodeTo(transitionModelForClone.getNodeTo());
     * nodeModel.addTransition(transitionModel); }
     * 
     * if (diagramModel.isShowActions()) {
     * nodeModel.setActionsCount(GraphImageHelper.getNodeActionsCount(node)); }
     * GraphImageHelper.initNodeModel(node, nodeModel);
     * 
     * nodeModel.setX(NodeLayoutData.get(root).getX());
     * nodeModel.setY(NodeLayoutData.get(root).getY()); if
     * (NodeLayoutData.get(root).getPreferredWidth() > 0) {
     * nodeModel.setWidth(NodeLayoutData.get(root).getPreferredWidth()); } //
     * nodeModel.setType(data.getNodeType(nodeId)); if (nodeModel.getType() ==
     * NodeType.FORK || nodeModel.getType() == NodeType.JOIN) { if
     * (nodeModel.getHeight() > 4) { nodeModel.setHeight(4); } } AbstractFigure
     * nodeFigure = factory.createFigure(nodeModel, false);
     * allNodeFigures.put(root, nodeFigure); nodeFigures.put(nodeFigure, new
     * RenderHits(DrawProperties.getBaseColor()));
     * 
     * NodeEnterLog nodeEnterLog = root.getNodeEnterLog(); if (nodeEnterLog !=
     * null) { addedTooltipOnGraph(node, nodeModel, nodeEnterLog); }
     * 
     * for (HistoryGraphTransitionModel historyTransition :
     * root.getTransitions()) {
     * calculateCoordinatesForNodes(historyTransition.getToNode(), visited); for
     * (Transition transition : node.getLeavingTransitions()) { if
     * (!historyTransition.getNodeId().equals(transition.getNodeId())) {
     * continue; }
     * 
     * TransitionModel transitionModel =
     * nodeModel.getTransition(transition.getName());
     * transitionModel.getBendpoints().clear();
     * 
     * TransitionLog transitionLog = historyTransition.getLog(); if
     * (transitionLog != null) {
     * transitionModel.setName(CalendarUtil.formatDateTime
     * (transitionLog.getCreateDate())); }
     * 
     * if (diagramModel.isShowActions()) {
     * transitionModel.setActionsCount(GraphImageHelper
     * .getTransitionActionsCount(transition)); }
     * 
     * AbstractFigure figureTo =
     * allNodeFigures.get(historyTransition.getToNode());
     * 
     * if (figureTo != null) {
     * 
     * if (data.getNodeType(nodeModel.getNodeId()) == NodeType.FORK) {
     * BendpointModel bendpointModel = new BendpointModel();
     * bendpointModel.setX(figureTo.getCoords()[0] + figureTo.getCoords()[2] /
     * 2); bendpointModel.setY(nodeModel.getY() + nodeModel.getHeight() / 2);
     * transitionModel.addBendpoint(bendpointModel); } if
     * (data.getNodeType(nodeModel.getNodeId()) == NodeType.JOIN) {
     * BendpointModel bendpointModel = new BendpointModel();
     * bendpointModel.setX(nodeModel.getX() + nodeModel.getWidth() / 2);
     * bendpointModel.setY(figureTo.getCoords()[1]);
     * transitionModel.addBendpoint(bendpointModel); }
     * 
     * TransitionFigureBase transitionFigureBase =
     * factory.createTransitionFigure(transitionModel, nodeFigure, figureTo);
     * transitionFigureBase.init(transitionModel, nodeFigure, figureTo); if
     * (Objects.equal(nodeModel.getTimerTransitionName(),
     * transitionModel.getName())) {
     * transitionFigureBase.setTimerInfo(GraphImageHelper.getTimerInfo(node)); }
     * transitionFigureBases.put(transitionFigureBase, new
     * RenderHits(DrawProperties.getTransitionColor())); } } } }
     */
}

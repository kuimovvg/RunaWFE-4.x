package ru.runa.wfe.graph.history.figure;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.history.GraphImageHelper;
import ru.runa.wfe.graph.history.RenderHits;
import ru.runa.wfe.graph.history.figure.uml.UMLFigureFactory;
import ru.runa.wfe.graph.history.model.BendpointModel;
import ru.runa.wfe.graph.history.model.DiagramModel;
import ru.runa.wfe.graph.history.model.NodeModel;
import ru.runa.wfe.graph.history.model.TransitionModel;
import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphGenericNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphJoinNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.graph.HistoryGraphNodeVisitor;
import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;
import ru.runa.wfe.history.layout.NodeLayoutData;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

import com.google.common.base.Objects;

/**
 * Operation to create figures for history graph painting.
 */
public class CreateGraphFigures implements HistoryGraphNodeVisitor<CreateGraphFiguresContext> {

    private final DiagramModel diagramModel;
    private final AbstractFigureFactory factory = new UMLFigureFactory();

    public CreateGraphFigures(DiagramModel diagramModel) {
        super();
        this.diagramModel = diagramModel;
    }

    @Override
    public void onForkNode(HistoryGraphForkNodeModel node, CreateGraphFiguresContext context) {
        FiguresNodeData data = FiguresNodeData.getOrCreate(node);
        if (!data.isFiguresInitializeRequired()) {
            return;
        }
        NodeModel model = createCommonModel(node);
        createFigureForNode(node, data, model);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        createFigureForTransitions(node, data, model);
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, CreateGraphFiguresContext context) {
        FiguresNodeData data = FiguresNodeData.getOrCreate(node);
        if (!data.isFiguresInitializeRequired()) {
            return;
        }
        NodeModel model = createCommonModel(node);
        createFigureForNode(node, data, model);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        createFigureForTransitions(node, data, model);
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, CreateGraphFiguresContext context) {
        FiguresNodeData data = FiguresNodeData.getOrCreate(node);
        if (!data.isFiguresInitializeRequired()) {
            return;
        }
        NodeModel model = createCommonModel(node);
        createFigureForNode(node, data, model);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        createFigureForTransitions(node, data, model);
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, CreateGraphFiguresContext context) {
        FiguresNodeData data = FiguresNodeData.getOrCreate(node);
        if (!data.isFiguresInitializeRequired()) {
            return;
        }
        NodeModel model = createCommonModel(node);
        createFigureForNode(node, data, model);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        createFigureForTransitions(node, data, model);
    }

    private NodeModel createCommonModel(HistoryGraphNode historyNode) {
        NodeLayoutData nodeLayoutData = NodeLayoutData.get(historyNode);
        NodeModel nodeModelForClone = diagramModel.getNodeNotNull(historyNode.getNodeId());
        NodeModel nodeModel = new NodeModel();
        GraphImageHelper.initNodeModel(historyNode.getNode(), nodeModel);
        nodeModel.setNodeId(nodeModelForClone.getNodeId());
        nodeModel.setMinimizedView(nodeModelForClone.isMinimizedView());
        nodeModel.setTimerTransitionName(nodeModelForClone.getTimerTransitionName());
        nodeModel.setAsync(nodeModelForClone.isAsync());
        nodeModel.setSwimlane(nodeModelForClone.getSwimlane());
        nodeModel.setX(nodeLayoutData.getX());
        nodeModel.setY(nodeLayoutData.getY());
        nodeModel.setWidth(nodeLayoutData.getWidth());
        nodeModel.setHeight(nodeLayoutData.getHeight());
        if (diagramModel.isShowActions()) {
            nodeModel.setActionsCount(GraphImageHelper.getNodeActionsCount(historyNode.getNode()));
        }
        nodeModel.setType(getNodeType(historyNode));
        return nodeModel;
    }

    /**
     * Returns {@link NodeType} for node. EXCLUSIVE_GATEWAY is replaced with
     * DECISION and PARALLEL_GATEWAY replaced with FORK or JOIN
     * 
     * @param nodeId
     *            Node id.
     * @return
     */
    NodeType getNodeType(HistoryGraphNode historyGraphNode) {
        Node node = historyGraphNode.getNode();
        switch (node.getNodeType()) {
        case EXCLUSIVE_GATEWAY:
            return NodeType.DECISION;
        case PARALLEL_GATEWAY:
            return node.getLeavingTransitions().size() > 1 ? NodeType.FORK : NodeType.JOIN;
        default:
            return node.getNodeType();
        }
    }

    private void createFigureForNode(HistoryGraphNode node, FiguresNodeData data, NodeModel model) {
        AbstractFigure nodeFigure = factory.createFigure(model, false);
        data.setFigureData(nodeFigure, new RenderHits(DrawProperties.getBaseColor()), model);
    }

    private void createFigureForTransitions(HistoryGraphNode node, FiguresNodeData data, NodeModel model) {
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            createTransitionFigure(transition, data);
        }
    }

    private void createTransitionFigure(HistoryGraphTransitionModel transition, FiguresNodeData data) {
        FiguresNodeData fromNodeFigure = FiguresNodeData.getOrThrow(transition.getFromNode());
        FiguresNodeData toNodeFigure = FiguresNodeData.getOrThrow(transition.getToNode());
        TransitionModel transitionModel = new TransitionModel();
        transitionModel.setName(CalendarUtil.formatDateTime(transition.getLog().getCreateDate()));
        transitionModel.setNodeFrom(fromNodeFigure.getNodeModel());
        transitionModel.setNodeTo(toNodeFigure.getNodeModel());
        if (diagramModel.isShowActions()) {
            transitionModel.setActionsCount(GraphImageHelper.getTransitionActionsCount(transition.getTransition()));
        }
        if (getNodeType(transition.getFromNode()) == NodeType.FORK) {
            BendpointModel bendpointModel = new BendpointModel();
            bendpointModel.setX(toNodeFigure.getFigure().getCoords()[0] + toNodeFigure.getFigure().getCoords()[2] / 2);
            bendpointModel.setY(fromNodeFigure.getNodeModel().getY() + fromNodeFigure.getNodeModel().getHeight() / 2);
            transitionModel.addBendpoint(bendpointModel);
        }
        if (getNodeType(transition.getFromNode()) == NodeType.JOIN) {
            BendpointModel bendpointModel = new BendpointModel();
            bendpointModel.setX(fromNodeFigure.getNodeModel().getX() + fromNodeFigure.getNodeModel().getWidth() / 2);
            bendpointModel.setY(toNodeFigure.getFigure().getCoords()[1]);
            transitionModel.addBendpoint(bendpointModel);
        }

        TransitionFigureBase figure = factory.createTransitionFigure(transitionModel, fromNodeFigure.getFigure(), toNodeFigure.getFigure());
        figure.init(transitionModel, fromNodeFigure.getFigure(), toNodeFigure.getFigure());
        if (Objects.equal(fromNodeFigure.getNodeModel().getTimerTransitionName(), transitionModel.getName())) {
            figure.setTimerInfo(GraphImageHelper.getTimerInfo(transition.getFromNode().getNode()));
        }
        data.addTransition(figure, new RenderHits(DrawProperties.getTransitionColor()));
    }
}

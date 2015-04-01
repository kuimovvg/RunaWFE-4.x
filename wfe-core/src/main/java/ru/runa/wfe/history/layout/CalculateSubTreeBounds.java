package ru.runa.wfe.history.layout;

import java.util.Map;
import java.util.Stack;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.graph.history.model.DiagramModel;
import ru.runa.wfe.graph.history.model.NodeModel;
import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphGenericNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphJoinNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.graph.HistoryGraphNodeVisitor;
import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;

import com.google.common.collect.Maps;

/**
 * Calculates graph flow bounds. Going from bottom to top and calculating width
 * and height on every graph node.
 */
public class CalculateSubTreeBounds implements HistoryGraphNodeVisitor<Object> {

    /**
     * Stack for calculation fork->join relations.
     */
    private final Stack<HistoryGraphForkNodeModel> forkStack = new Stack<HistoryGraphForkNodeModel>();
    /**
     * Maps fork node to related join node.
     */
    private final Map<HistoryGraphForkNodeModel, HistoryGraphJoinNodeModel> forkToJoin = Maps.newHashMap();
    /**
     * Data about graphical elements size.
     */
    private final DiagramModel diagramModel;

    public CalculateSubTreeBounds(DiagramModel diagramModel) {
        super();
        this.diagramModel = diagramModel;
    }

    @Override
    public void onForkNode(HistoryGraphForkNodeModel node, Object o) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.subtreeCalulationRequired()) {
            return;
        }
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            forkStack.push(node);
            transition.getToNode().processBy(this, o);
            if (forkStack.peek() == node) {
                forkStack.pop();
            }
        }
        data.setSubtreeHeight(getSubtreeHeight(node) + HistoryGraphLayoutProperties.maxCellHeight);
        int width = 0;
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            width += getSubtreeWidthForParent(transition.getToNode());
        }
        data.setPreferredWidth(width - HistoryGraphLayoutProperties.widthBetweenNodes);
        HistoryGraphJoinNodeModel join = forkToJoin.get(node);
        if (join != null && NodeLayoutData.get(join).getSubtreeWidth() > width) {
            width = NodeLayoutData.get(join).getSubtreeWidth();
        }
        data.setSubtreeWidth(width);
        if (join != null) {
            NodeLayoutData.get(join).setSubtreeWidth(width);
            NodeLayoutData.get(join).setPreferredWidth(data.getPreferredWidth());
        }
        data.setHeight(HistoryGraphLayoutProperties.joinHeight);
        data.setWidth(0);
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, Object o) {
        if (forkStack.peek() == null) {
            throw new InternalApplicationException("Graph build error: fork is not registered; join registration failed");
        }
        forkToJoin.put(forkStack.pop(), node);
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.subtreeCalulationRequired()) {
            return;
        }
        HistoryGraphTransitionModel transition = node.getTransitions().size() == 0 ? null : node.getTransitions().get(0);
        if (transition != null) {
            transition.getToNode().processBy(this, o);
        }
        NodeModel nodeModel = diagramModel.getNodeNotNull(node.getNode().getNodeId());
        data.setSubtreeHeight(getSubtreeHeight(node) + HistoryGraphLayoutProperties.maxCellHeight);
        int width = transition == null ? nodeModel.getWidth() + HistoryGraphLayoutProperties.widthBetweenNodes : getSubtreeWidthForParent(transition
                .getToNode());
        data.setSubtreeWidth(width);
        data.setHeight(HistoryGraphLayoutProperties.joinHeight);
        data.setWidth(0);
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, Object o) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.subtreeCalulationRequired()) {
            return;
        }
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, o);
        }
        NodeModel nodeModel = diagramModel.getNodeNotNull(node.getNode().getNodeId());
        data.setSubtreeHeight(getSubtreeHeight(node) + HistoryGraphLayoutProperties.maxCellHeight);
        int width = 0;
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            width += getSubtreeWidthForParent(transition.getToNode());
        }
        int nodeWidth = nodeModel.getWidth() + HistoryGraphLayoutProperties.widthBetweenNodes;
        int subtreeWidth = width < nodeWidth ? nodeWidth : width;
        data.setSubtreeWidth(subtreeWidth);
        data.setPreferredWidth(subtreeWidth - HistoryGraphLayoutProperties.widthBetweenNodes);
        data.setHeight(HistoryGraphLayoutProperties.joinHeight);
        data.setWidth(0);
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, Object o) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.subtreeCalulationRequired()) {
            return;
        }
        HistoryGraphTransitionModel transition = node.getTransitions().size() == 0 ? null : node.getTransitions().get(0);
        if (transition != null) {
            transition.getToNode().processBy(this, o);
        }
        NodeModel nodeModel = diagramModel.getNodeNotNull(node.getNode().getNodeId());
        data.setSubtreeHeight(getSubtreeHeight(node) + HistoryGraphLayoutProperties.maxCellHeight);
        int treeWidth = transition == null ? 0 : getSubtreeWidthForParent(transition.getToNode());
        int nodeWidth = nodeModel.getWidth() + HistoryGraphLayoutProperties.widthBetweenNodes;
        data.setSubtreeWidth(treeWidth < nodeWidth ? nodeWidth : treeWidth);
        data.setPreferredWidth(nodeModel.getWidth());
        data.setHeight(nodeModel.getHeight());
        data.setWidth(nodeModel.getWidth());
    }

    /**
     * Calculates max height of node subtrees.
     * 
     * @param node
     *            Node, which subtree height is calculated.
     * @return Returns max subtree height.
     */
    private int getSubtreeHeight(HistoryGraphNode node) {
        int height = 0;
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            if (height < NodeLayoutData.get(transition.getToNode()).getSubtreeHeight()) {
                height = NodeLayoutData.get(transition.getToNode()).getSubtreeHeight();
            }
        }
        return height;
    }

    /**
     * Get subtree width for parent width calculation. We must return 0 on join
     * and parallel gateways, because of this nodes aggregates several flows
     * from top and it's width must not be considered in top flows.
     * 
     * @param node
     *            Node, which width must be returned for parent width
     *            calculation.
     * @return Returns node width for parent width calculation.
     */
    private int getSubtreeWidthForParent(HistoryGraphNode node) {
        if (node instanceof HistoryGraphJoinNodeModel) {
            return 0;
        }
        if (node instanceof HistoryGraphParallelNodeModel && !((HistoryGraphParallelNodeModel) node).isForkNode()) {
            return 0;
        }
        return NodeLayoutData.get(node).getSubtreeWidth();
    }
}

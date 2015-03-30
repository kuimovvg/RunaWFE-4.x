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
        if (!data.widthHeightCalulationRequired()) {
            return;
        }
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            forkStack.push(node);
            transition.getToNode().processBy(this, o);
        }
        int height = 0;
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            if (height < NodeLayoutData.get(transition.getToNode()).getSubtreeHeight()) {
                height = NodeLayoutData.get(transition.getToNode()).getSubtreeHeight();
            }
        }
        data.setSubtreeHeight(height + HistoryGraphLayoutProperties.maxNodeHeight);
        int prefferedWidth = 0;
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            prefferedWidth += getSubtreeWidthForParent(transition.getToNode());
        }
        data.setPreferredWidth(prefferedWidth - HistoryGraphLayoutProperties.widthBetweenNodes);
        int width = data.getPreferredWidth() + HistoryGraphLayoutProperties.widthBetweenNodes;
        HistoryGraphJoinNodeModel join = forkToJoin.get(node);
        if (join != null && NodeLayoutData.get(join).getSubtreeWidth() > width) {
            width = NodeLayoutData.get(join).getSubtreeWidth();
        }
        data.setSubtreeWidth(width);
        if (join != null) {
            NodeLayoutData.get(join).setSubtreeWidth(width);
            NodeLayoutData.get(join).setPreferredWidth(data.getPreferredWidth());
        }
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, Object o) {
        if (forkStack.peek() == null) {
            throw new InternalApplicationException("Graph build error: fork is not registered; join registration failed");
        }
        HistoryGraphForkNodeModel forkNodeModel = forkStack.pop();
        forkToJoin.put(forkNodeModel, node);
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.widthHeightCalulationRequired()) {
            return;
        }
        HistoryGraphTransitionModel transition = node.getTransitions().size() == 0 ? null : node.getTransitions().get(0);
        if (transition != null) {
            transition.getToNode().processBy(this, o);
        }
        int height = transition == null ? 0 : NodeLayoutData.get(transition.getToNode()).getSubtreeHeight();
        NodeModel nodeModel = diagramModel.getNodeNotNull(node.getNode().getNodeId());
        data.setSubtreeHeight(height + HistoryGraphLayoutProperties.maxNodeHeight);
        int width = transition == null ? nodeModel.getWidth() + HistoryGraphLayoutProperties.widthBetweenNodes : getSubtreeWidthForParent(transition
                .getToNode());
        data.setSubtreeWidth(width);
        data.setPreferredWidth(nodeModel.getWidth());
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, Object o) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.widthHeightCalulationRequired()) {
            return;
        }
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, o);
        }
        int height = 0;
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            if (height < NodeLayoutData.get(transition.getToNode()).getSubtreeHeight()) {
                height = NodeLayoutData.get(transition.getToNode()).getSubtreeHeight();
            }
        }
        NodeModel nodeModel = diagramModel.getNodeNotNull(node.getNode().getNodeId());
        data.setSubtreeHeight(height + HistoryGraphLayoutProperties.maxNodeHeight);
        int width = 0;
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            width += getSubtreeWidthForParent(transition.getToNode());
        }
        int nodeWidth = nodeModel.getWidth() + HistoryGraphLayoutProperties.widthBetweenNodes;
        int subtreeWidth = width < nodeWidth ? nodeWidth : width;
        data.setSubtreeWidth(subtreeWidth);
        data.setPreferredWidth(subtreeWidth - HistoryGraphLayoutProperties.widthBetweenNodes);
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, Object o) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.widthHeightCalulationRequired()) {
            return;
        }
        HistoryGraphTransitionModel transition = node.getTransitions().size() == 0 ? null : node.getTransitions().get(0);
        if (transition != null) {
            transition.getToNode().processBy(this, o);
        }
        int height = transition == null ? 0 : NodeLayoutData.get(transition.getToNode()).getSubtreeHeight();
        NodeModel nodeModel = diagramModel.getNodeNotNull(node.getNode().getNodeId());
        data.setSubtreeHeight(height + HistoryGraphLayoutProperties.maxNodeHeight);
        int treeWidth = transition == null ? 0 : getSubtreeWidthForParent(transition.getToNode());
        int nodeWidth = nodeModel.getWidth() + HistoryGraphLayoutProperties.widthBetweenNodes;
        data.setSubtreeWidth(treeWidth < nodeWidth ? nodeWidth : treeWidth);
        data.setPreferredWidth(nodeModel.getWidth());
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

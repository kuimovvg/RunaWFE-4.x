package ru.runa.wfe.graph.history.figure;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.graph.history.GraphImage.RenderHits;
import ru.runa.wfe.graph.history.model.NodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNode;

/**
 * Node custom data with figures, requires to paint history graph.
 */
public class FiguresNodeData {
    /**
     * Key within node custom data to store figures information.
     */
    static final String DATA_KEY = "FIGURES";

    /**
     * Flag, equals true, if figures already initialized and false otherwise.
     */
    private boolean isFiguresInitialized = false;
    /**
     * Model for figure at this node.
     */
    private NodeModel nodeModel;
    /**
     * Figure to draw this node.
     */
    private AbstractFigure figure;
    /**
     * Render hits for figure.
     */
    private RenderHits renderHits;
    /**
     * Figures for leaving transition.
     */
    private final List<TransitionFigureData> transitions = new ArrayList<TransitionFigureData>();

    public boolean isFiguresInitializeRequired() {
        boolean initialized = isFiguresInitialized;
        isFiguresInitialized = true;
        return !initialized;
    }

    public AbstractFigure getFigure() {
        return figure;
    }

    public void setFigure(AbstractFigure figure) {
        this.figure = figure;
    }

    public void setFigureData(AbstractFigure figure, RenderHits renderHits, NodeModel nodeModel) {
        this.nodeModel = nodeModel;
        this.figure = figure;
        this.renderHits = renderHits;
    }

    public RenderHits getRenderHits() {
        return renderHits;
    }

    public void setRenderHits(RenderHits renderHits) {
        this.renderHits = renderHits;
    }

    public NodeModel getNodeModel() {
        return nodeModel;
    }

    public void setNodeModel(NodeModel nodeModel) {
        this.nodeModel = nodeModel;
    }

    public List<TransitionFigureData> getTransitions() {
        return transitions;
    }

    public void addTransition(TransitionFigureBase transition, RenderHits renderHits) {
        transitions.add(new TransitionFigureData(transition, renderHits));
    }

    /**
     * Get figures data information or creates and stores it in node if not
     * available.
     * 
     * @param node
     *            Node to get data.
     * @return Returns figures data.
     */
    public static FiguresNodeData getOrCreate(HistoryGraphNode node) {
        FiguresNodeData data = (FiguresNodeData) node.getCustomData().get(DATA_KEY);
        if (data == null) {
            data = new FiguresNodeData();
            node.getCustomData().put(DATA_KEY, data);
        }
        return data;
    }

    /**
     * Get figures data information or throws exception if no data available.
     * 
     * @param node
     *            Node to get data.
     * @return Returns figures data.
     */
    public static FiguresNodeData getOrThrow(HistoryGraphNode node) {
        FiguresNodeData data = (FiguresNodeData) node.getCustomData().get(DATA_KEY);
        if (data == null) {
            throw new InternalApplicationException("figure data is not available.");
        }
        return data;
    }

    public final class TransitionFigureData {
        private final TransitionFigureBase transitionFigure;
        private final RenderHits renderHits;

        public TransitionFigureData(TransitionFigureBase transitionFigure, RenderHits renderHits) {
            super();
            this.transitionFigure = transitionFigure;
            this.renderHits = renderHits;
        }

        public TransitionFigureBase getTransitionFigure() {
            return transitionFigure;
        }

        public RenderHits getRenderHits() {
            return renderHits;
        }
    }
}

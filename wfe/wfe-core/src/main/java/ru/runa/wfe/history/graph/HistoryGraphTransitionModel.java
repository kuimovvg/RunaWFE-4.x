package ru.runa.wfe.history.graph;

import ru.runa.wfe.audit.TransitionLog;

/**
 * History graph model for transition between nodes.
 */
public class HistoryGraphTransitionModel {

    /**
     * History graph node, from which transition leave.
     */
    private final HistoryGraphNode fromNode;
    /**
     * History graph node, which accept transition.
     */
    private final HistoryGraphNode toNode;
    /**
     * Log instance for current transition.
     */
    private final TransitionLog log;

    public HistoryGraphTransitionModel(HistoryGraphNode fromNode, HistoryGraphNode toNode, TransitionLog log) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.log = log;
    }

    /**
     * Get history graph node, which accept transition.
     * 
     * @return Returns history graph node, which accept transition.
     */
    public HistoryGraphNode getToNode() {
        return toNode;
    }

    /**
     * Get history graph node, from which transition leave.
     * 
     * @return Returns history graph node, from which transition leave.
     */
    public HistoryGraphNode getFromNode() {
        return fromNode;
    }

    /**
     * Get node id for transition log instance.
     * 
     * @return Returns node id for transition log instance.
     */
    public String getNodeId() {
        return log.getNodeId();
    }

    /**
     * Get transition log instance.
     * 
     * @return Returns transition log instance.
     */
    public TransitionLog getLog() {
        return log;
    }
}

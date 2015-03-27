package ru.runa.wfe.graph.history;

import java.util.HashMap;
import java.util.HashSet;

import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubProcessState;
import ru.runa.wfe.lang.SubprocessDefinition;

public class ProcessDefinitionData {
    /**
     * Process definition.
     */
    private final ProcessDefinition processDefinition;
    /**
     * Maps from node id to node model.
     */
    private final HashMap<String, Node> processDefinitionNodes = new HashMap<String, Node>();
    /**
     * Nodes id, which creates additional tokens (forks, parallel gateway's).
     */
    private final HashSet<String> createTokenNodes = new HashSet<String>();
    /**
     * Embedded subprocesses of this process definition.
     */
    private final HashSet<String> subProcesses = new HashSet<String>();

    public ProcessDefinitionData(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        for (Node node : processDefinition.getNodes(true)) {
            this.processDefinitionNodes.put(node.getNodeId(), node);
            if (node.getNodeType() == NodeType.FORK || node.getNodeType() == NodeType.PARALLEL_GATEWAY) {
                getCreateTokenNodes().add(node.getNodeId());
            }
            if (node.getNodeType() == NodeType.SUBPROCESS && ((SubProcessState) node).isEmbedded()) {
                getSubProcesses().add(getEmbeddedSubprocess(((SubProcessState) node).getSubProcessName()).getNodeId());
            }
        }
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public Node getNode(String nodeId) {
        return processDefinitionNodes.get(nodeId);
    }

    public SubprocessDefinition getEmbeddedSubprocess(String subProcessName) {
        return processDefinition.getEmbeddedSubprocessByNameNotNull(subProcessName);
    }

    public HashSet<String> getCreateTokenNodes() {
        return createTokenNodes;
    }

    public HashSet<String> getSubProcesses() {
        return subProcesses;
    }

    public String checkEmbeddedSubprocess(String nodeId) {
        int dotPos = nodeId.indexOf('.');
        if (dotPos == -1) {
            return null;
        }
        String subProcessName = nodeId.substring(0, dotPos);
        return subProcesses.contains(subProcessName) ? subProcessName : null;
    }
}

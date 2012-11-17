package ru.runa.wfe.graph.image;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.graph.image.model.DiagramModel;
import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.graph.view.BaseGraphElementPresentation;
import ru.runa.wfe.graph.view.DecisionGraphElementPresentation;
import ru.runa.wfe.graph.view.EndStateGraphElementPresentation;
import ru.runa.wfe.graph.view.ForkGraphElementPresentation;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.JoinGraphElementPresentation;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.NodeGraphElementPresentation;
import ru.runa.wfe.graph.view.ReceiveMessageGraphElementPresentation;
import ru.runa.wfe.graph.view.SendMessageGraphElementPresentation;
import ru.runa.wfe.graph.view.StartStateGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;
import ru.runa.wfe.graph.view.WaitStateGraphElementPresentation;
import ru.runa.wfe.lang.MultiProcessState;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubProcessState;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;

public class GraphElementPresentationBuilder {

    /**
     * Convert nodes to graph elements.
     * 
     * @param definitionNodes
     *            Nodes to convert
     * @return List of graph elements for nodes.
     */
    public static List<GraphElementPresentation> createElements(ProcessDefinition definition) throws Exception {
        DiagramModel diagramModel = DiagramModel.load(definition.getFileDataNotNull(IFileDataProvider.GPD_XML_FILE_NAME));
        List<GraphElementPresentation> result = new ArrayList<GraphElementPresentation>();
        for (Node node : definition.getNodes()) {
            NodeModel model = diagramModel.getNode(node.getNodeId());
            BaseGraphElementPresentation presentation;
            switch (node.getNodeType()) {
            case SubProcess:
                presentation = new SubprocessGraphElementPresentation(((SubProcessState) node).getSubProcessName());
                break;
            case MultiInstance:
                presentation = new MultiinstanceGraphElementPresentation(((MultiProcessState) node).getSubProcessName());
                break;
            case Task:
                TaskDefinition taskDefinition = ((TaskNode) node).getFirstTaskNotNull();
                presentation = new TaskGraphElementPresentation(taskDefinition.getSwimlane().getName(), model.isMinimizedView());
                break;
            case WaitState:
                presentation = new WaitStateGraphElementPresentation();
                break;
            case StartState:
                presentation = new StartStateGraphElementPresentation();
                break;
            case EndState:
                presentation = new EndStateGraphElementPresentation();
                break;
            case Fork:
                presentation = new ForkGraphElementPresentation();
                break;
            case Join:
                presentation = new JoinGraphElementPresentation();
                break;
            case Decision:
                presentation = new DecisionGraphElementPresentation();
                break;
            case ActionNode:
                presentation = new NodeGraphElementPresentation();
                break;
            case SendMessage:
                presentation = new SendMessageGraphElementPresentation();
                break;
            case ReceiveMessage:
                presentation = new ReceiveMessageGraphElementPresentation();
                break;
            default:
                throw new InternalApplicationException("Unexpected element " + node.getNodeType());
            }
            presentation.setNodeId(node.getNodeId());
            presentation.setName(node.getName());
            presentation.setGraphConstraints(model.getConstraints());
            result.add(presentation);
        }
        return result;
    }
}

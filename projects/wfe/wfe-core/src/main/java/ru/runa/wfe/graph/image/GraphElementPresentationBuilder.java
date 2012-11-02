package ru.runa.wfe.graph.image;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.graph.image.model.DiagramModel;
import ru.runa.wfe.graph.image.model.NodeModel;
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
            NodeModel model = diagramModel.getNode(node.getName());
            switch (node.getNodeType()) {
            case SubProcess:
                result.add(new SubprocessGraphElementPresentation(node.getName(), ((SubProcessState) node).getSubProcessName(), model
                        .getConstraints()));
                break;
            case MultiInstance:
                result.add(new MultiinstanceGraphElementPresentation(node.getName(), ((MultiProcessState) node).getSubProcessName(), model
                        .getConstraints()));
                break;
            case Task:
                TaskDefinition taskDefinition = ((TaskNode) node).getFirstTaskNotNull();
                result.add(new TaskGraphElementPresentation(node.getName(), model.getConstraints(), taskDefinition.getSwimlane().getName(), model
                        .isMinimizedView()));
                break;
            case WaitState:
                result.add(new WaitStateGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case StartState:
                result.add(new StartStateGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case EndState:
                result.add(new EndStateGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case Fork:
                result.add(new ForkGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case Join:
                result.add(new JoinGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case Decision:
                result.add(new DecisionGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case ActionNode:
                result.add(new NodeGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case SendMessage:
                result.add(new SendMessageGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            case ReceiveMessage:
                result.add(new ReceiveMessageGraphElementPresentation(node.getName(), model.getConstraints()));
                break;
            default:
                break;
            }
        }
        return result;
    }

}

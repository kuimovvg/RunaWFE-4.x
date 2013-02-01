package ru.runa.wfe.graph.image;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.graph.image.model.DiagramModel;
import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;

public class GraphElementPresentationBuilder {

    /**
     * Convert nodes to graph elements.
     * 
     * @param definitionNodes
     *            Nodes to convert
     * @return List of graph elements for nodes.
     */
    public static List<GraphElementPresentation> createElements(ProcessDefinition definition) {
        DiagramModel diagramModel = DiagramModel.load(definition);
        List<GraphElementPresentation> result = new ArrayList<GraphElementPresentation>();
        for (Node node : definition.getNodes()) {
            NodeModel model = diagramModel.getNode(node.getNodeId());
            GraphElementPresentation presentation;
            switch (node.getNodeType()) {
            case Subprocess:
                presentation = new SubprocessGraphElementPresentation();
                break;
            case MultiSubprocess:
                presentation = new MultiinstanceGraphElementPresentation();
                break;
            case TaskNode:
                presentation = new TaskGraphElementPresentation();
                break;
            default:
                presentation = new GraphElementPresentation();
            }
            presentation.initialize(node, model);
            presentation.setGraphConstraints(model.getConstraints());
            result.add(presentation);
        }
        return result;
    }
}

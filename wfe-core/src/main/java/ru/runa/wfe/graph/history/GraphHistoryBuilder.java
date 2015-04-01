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

/**
 * History graph building and creating tooltip for elements.
 */
public class GraphHistoryBuilder {

    private final DiagramModel diagramModel;
    private final GraphHistoryBuilderData data;

    public GraphHistoryBuilder(List<Executor> executors, Process processInstance, ProcessDefinition processDefinition,
            List<ProcessLog> fullProcessLogs, String subProcessId) {
        this.data = new GraphHistoryBuilderData(executors, processInstance, processDefinition, fullProcessLogs, subProcessId);
        diagramModel = (subProcessId != null && !"null".equals(subProcessId)) ? DiagramModel.load(processDefinition
                .getEmbeddedSubprocessByIdNotNull(subProcessId)) : DiagramModel.load(processDefinition);
    }

    /**
     * Creates graph history as image.
     * 
     * @return Returns image bytes.
     */
    public byte[] createDiagram() throws Exception {
        HistoryGraphNode root = BuildHistoryGraph();
        root.processBy(new CreateGraphFigures(diagramModel), new CreateGraphFiguresContext());
        CreateHistoryGraphImage createImageOperation = new CreateHistoryGraphImage(diagramModel);
        root.processBy(createImageOperation, new CreateHistoryGraphImageContext());
        return createImageOperation.getImageBytes();
    }

    /**
     * Creates tooltip presentation elements for graph history.
     * 
     * @return Returns list of tooltips for history graph.
     */
    public List<GraphElementPresentation> getPresentations() throws Exception {
        HistoryGraphNode root = BuildHistoryGraph();
        CreateGraphElementPresentation createPresentationOperation = new CreateGraphElementPresentation(data);
        root.processBy(createPresentationOperation, new CreateGraphElementPresentationContext());
        return createPresentationOperation.getPresentationElements();
    }

    /**
     * Creates and layouts history graph. Now it's ready to draw into image or
     * create tooltip elements.
     * 
     * @return Return created history graph.
     */
    private HistoryGraphNode BuildHistoryGraph() {
        HistoryGraphNode root = HistoryGraphBuilder.buildHistoryGraph(data.getProcessLogs(), data.getProcessDefinitionData());
        root.processBy(new CalculateSubTreeBounds(diagramModel), null);
        root.processBy(new PushWidthDown(), -1);
        root.processBy(new TransitionOrderer(), new TransitionOrdererContext());
        root.processBy(new CalculateGraphLayout(), new CalculateGraphLayoutContext(NodeLayoutData.get(root).getSubtreeHeight()));
        diagramModel.setHeight(NodeLayoutData.get(root).getSubtreeHeight());
        diagramModel.setWidth(NodeLayoutData.get(root).getSubtreeWidth());
        return root;
    }
}

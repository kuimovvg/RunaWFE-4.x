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
package ru.runa.wfe.graph.history.figure.bpmn;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.graph.history.figure.AbstractFigure;
import ru.runa.wfe.graph.history.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.history.figure.TransitionFigureBase;
import ru.runa.wfe.graph.history.model.NodeModel;
import ru.runa.wfe.graph.history.model.TransitionModel;

public class BPMNFigureFactory extends AbstractFigureFactory {
    private final boolean graphiti;

    public BPMNFigureFactory(boolean graphiti) {
        this.graphiti = graphiti;
    }

    @Override
    public AbstractFigure createFigure(NodeModel nodeModel, boolean useEgdingOnly) {
        AbstractFigure figure = null;
        switch (nodeModel.getType()) {
        case TASK_STATE:
            figure = new TaskNodeFigure();
            ((TaskNodeFigure) figure).setGraphiti(graphiti);
            break;
        // TODO MultiTaskNode
        case EXCLUSIVE_GATEWAY:
            figure = new Rhomb("image/bpmn/decision.png");
            break;
        case PARALLEL_GATEWAY:
            figure = new Rhomb("image/bpmn/fork_join.png");
            break;
        case START_EVENT:
            figure = new Circle("image/bpmn/start.png");
            break;
        case END_PROCESS:
            figure = new Circle("image/bpmn/end.png");
            break;
        case END_TOKEN:
            figure = new Circle("image/bpmn/endtoken.png");
            break;
        case SUBPROCESS:
            figure = new SubprocessRect();
            break;
        case ACTION_NODE:
            // if (graphiti) {
            // figure = new RoundedRect("image/bpmn/script.png");
            // } else {
            // figure = new RoundedRect(null);
            // }
            figure = new RoundedRect(null);
            break;
        case WAIT_STATE:
            figure = new Circle("image/bpmn/waitstate.png");
            break;
        case MULTI_SUBPROCESS:
            figure = new SubprocessRect();
            break;
        case SEND_MESSAGE:
            figure = new Circle("image/bpmn/sendmessage.png");
            break;
        case RECEIVE_MESSAGE:
            figure = new Circle("image/bpmn/receivemessage.png");
            break;
        case TEXT_ANNOTATION:
            figure = new TextAnnotationFigure();
            break;
        default:
            throw new InternalApplicationException("Unexpected figure type found: " + nodeModel.getType());
        }
        figure.initFigure(nodeModel, useEgdingOnly);
        return figure;
    }

    @Override
    public TransitionFigureBase createTransitionFigure(TransitionModel transitionModel, AbstractFigure figureFrom, AbstractFigure figureTo) {
        return new TransitionFigureBase();
    }
}

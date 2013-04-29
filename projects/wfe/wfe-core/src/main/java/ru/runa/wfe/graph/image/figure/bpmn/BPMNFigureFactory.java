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
package ru.runa.wfe.graph.image.figure.bpmn;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigureBase;
import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.graph.image.model.TransitionModel;

public class BPMNFigureFactory extends AbstractFigureFactory {
    private final boolean graphiti;

    public BPMNFigureFactory(boolean graphiti) {
        this.graphiti = graphiti;
    }

    @Override
    public AbstractFigure createFigure(NodeModel nodeModel) {
        AbstractFigure figure = null;
        switch (nodeModel.getType()) {
        case TaskNode:
            figure = new TaskNodeFigure();
            ((TaskNodeFigure) figure).setGraphiti(graphiti);
            break;
        // TODO MultiTaskNode
        case Decision:
        case Merge:
            figure = new Rhomb("image/bpmn/decision.png");
            break;
        case Fork:
        case Join:
            figure = new Rhomb("image/bpmn/fork_join.png");
            break;
        case StartState:
            figure = new Circle("image/bpmn/start.png");
            break;
        case End:
            figure = new Circle("image/bpmn/end.png");
            break;
        case EndToken:
            figure = new Circle("image/bpmn/endtoken.png");
            break;
        case Subprocess:
            figure = new SubprocessRect();
            break;
        case ActionNode:
            if (graphiti) {
                figure = new Circle("image/bpmn/servicetask.png");
            } else {
                figure = new RoundedRect(null);
            }
            break;
        case WaitState:
            figure = new Circle("image/bpmn/waitstate.png");
            break;
        case MultiSubprocess:
            figure = new SubprocessRect();
            break;
        case SendMessage:
            figure = new Circle("image/bpmn/sendmessage.png");
            break;
        case ReceiveMessage:
            figure = new Circle("image/bpmn/receivemessage.png");
            break;
        default:
            throw new InternalApplicationException("Unexpected figure type found: " + nodeModel.getType());
        }
        figure.initFigure(nodeModel);
        return figure;
    }

    @Override
    public TransitionFigureBase createTransitionFigure(TransitionModel transitionModel, AbstractFigure figureFrom, AbstractFigure figureTo) {
        return new TransitionFigureBase();
    }
}

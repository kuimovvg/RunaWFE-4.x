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
package ru.runa.wfe.graph.image.figure;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.graph.image.figure.bpmn.Circle;
import ru.runa.wfe.graph.image.figure.bpmn.Rhomb;
import ru.runa.wfe.graph.image.figure.bpmn.RoundedRect;
import ru.runa.wfe.graph.image.figure.bpmn.SubprocessRect;
import ru.runa.wfe.graph.image.figure.bpmn.TaskFigure;
import ru.runa.wfe.graph.image.figure.uml.ActionNodeFigure;
import ru.runa.wfe.graph.image.figure.uml.DecisionFigure;
import ru.runa.wfe.graph.image.figure.uml.EndStateFigure;
import ru.runa.wfe.graph.image.figure.uml.ForkJoinFigure;
import ru.runa.wfe.graph.image.figure.uml.MultiInstanceStateFigure;
import ru.runa.wfe.graph.image.figure.uml.ProcessStateFigure;
import ru.runa.wfe.graph.image.figure.uml.ReceiveMessageNodeFigure;
import ru.runa.wfe.graph.image.figure.uml.SendMessageNodeFigure;
import ru.runa.wfe.graph.image.figure.uml.StartStateFigure;
import ru.runa.wfe.graph.image.figure.uml.StateFigure;
import ru.runa.wfe.graph.image.figure.uml.UMLTransition;
import ru.runa.wfe.graph.image.figure.uml.WaitStateFigure;
import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.graph.image.model.TransitionModel;

public class FigureFactory {
    private final boolean bpmn;
    private final boolean graphiti;

    public FigureFactory(boolean bpmn, boolean graphiti) {
        this.bpmn = bpmn;
        this.graphiti = graphiti;
    }

    public AbstractFigure createFigure(NodeModel nodeModel) {
        AbstractFigure figure = null;
        switch (nodeModel.getType()) {
        case NodeModel.STATE:
            figure = bpmn ? new TaskFigure(false) : new StateFigure(false);
            break;
        case NodeModel.STATE_WITH_TIMER:
            figure = bpmn ? new TaskFigure(true) : new StateFigure(true);
            break;
        case NodeModel.DECISION:
            figure = bpmn ? new Rhomb("decision.png") : new DecisionFigure();
            break;
        case NodeModel.FORK_JOIN:
            figure = bpmn ? new Rhomb("fork_join.png") : new ForkJoinFigure();
            break;
        case NodeModel.START_STATE:
            figure = bpmn ? new Circle("start.png") : new StartStateFigure();
            break;
        case NodeModel.END_STATE:
            figure = bpmn ? new Circle("end.png") : new EndStateFigure();
            break;
        case NodeModel.PROCESS_STATE:
            figure = bpmn ? new SubprocessRect() : new ProcessStateFigure();
            break;
        case NodeModel.ACTION_NODE:
            figure = bpmn ? new RoundedRect(null) : new ActionNodeFigure();
            break;
        case NodeModel.WAIT_STATE:
            figure = bpmn ? new Circle("waitstate.png") : new WaitStateFigure();
            break;
        case NodeModel.MULTI_PROCESS_STATE:
            figure = bpmn ? new SubprocessRect() : new MultiInstanceStateFigure();
            break;
        case NodeModel.SEND_MESSAGE:
            figure = bpmn ? new Circle("sendmessage.png") : new SendMessageNodeFigure();
            break;
        case NodeModel.RECEIVE_MESSAGE:
            figure = bpmn ? new Circle("receivemessage.png") : new ReceiveMessageNodeFigure();
            break;
        default:
            throw new InternalApplicationException("Unexpected figure type found: " + nodeModel.getType());
        }
        if (nodeModel.isMinimizedView()) {
            if (figure instanceof StateFigure) {
                ((StateFigure) figure).setMinimized(nodeModel.isMinimizedView());
            }
            if (figure instanceof TaskFigure) {
                ((TaskFigure) figure).setMinimized(nodeModel.isMinimizedView());
            }
        }
        if (figure instanceof TaskFigure) {
            ((TaskFigure) figure).setGraphiti(graphiti);
        }
        figure.initFigure(nodeModel);
        return figure;
    }

    public TransitionFigure createTransitionFigure(TransitionModel transitionModel, AbstractFigure figureFrom, AbstractFigure figureTo) {
        TransitionFigure figure = bpmn ? new TransitionFigure() : new UMLTransition();
        figure.init(transitionModel, figureFrom, figureTo);
        return figure;
    }
}

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
package ru.runa.wf.graph.figure.uml;

import java.awt.Point;
import java.awt.Rectangle;

import ru.runa.wf.graph.figure.TransitionFigure;
import ru.runa.wf.graph.model.BendpointModel;
import ru.runa.wf.graph.model.NodeModel;

public class UMLTransition extends TransitionFigure {

    @Override
    protected Point getCompatibleBendpoint(BendpointModel bendpointModel) {
        // TODO Used for compatibility
        Point center = figureFrom.getBendpoint();
        if ((center != null) && (figureFrom instanceof StateFigure)) {
            return bendpointModel.getPointFrom(center);
        } else if ((figureFrom instanceof ForkJoinFigure) && (figureTo instanceof ForkJoinFigure)) {
            Point result = bendpointModel.getPointFrom(center);
            int w1 = bendpointModel.getW1();
            int w2 = bendpointModel.getW2();
            int h1 = bendpointModel.getH1();
            int h2 = bendpointModel.getH2();

            int dw1 = w1 * h1 * (w1 - w2) / ((h1 - h2) * Math.abs(w1));

            result.x += dw1;

            return result;
        } else {
            center = figureTo.getBendpoint();
            return bendpointModel.getPointTo(center);
        }
    }

    @Override
    protected double[] getReferencePoint(Rectangle rectFrom, Rectangle rectTo) {
        double x;
        double y;
        if (figureTo.getType() == NodeModel.FORK_JOIN) {
            ForkJoinFigure forkJoin = (ForkJoinFigure) figureTo;
            if (!forkJoin.isVertical() && rectTo.contains(rectFrom.getCenterX(), rectTo.getCenterY())) {
                // horizontal ForkJoin
                x = rectFrom.getCenterX();
                y = rectTo.getCenterY();
            } else if (forkJoin.isVertical() && rectTo.contains(rectTo.getCenterX(), rectFrom.getCenterY())) {
                // vertical ForkJoin
                x = rectTo.getCenterX();
                y = rectFrom.getCenterY();
            } else {
                x = rectTo.getCenterX();
                y = rectTo.getCenterY();
            }
        } else {
            x = rectTo.getCenterX();
            y = rectTo.getCenterY();
        }
        return new double[] { x, y };
    }
}

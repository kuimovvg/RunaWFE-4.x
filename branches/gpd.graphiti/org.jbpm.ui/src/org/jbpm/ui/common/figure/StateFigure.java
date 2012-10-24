package org.jbpm.ui.common.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public abstract class StateFigure extends NodeFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        connectionAnchor = new StateAnchor(this);
    }

    @Override
    public Dimension getDefaultSize() {
        return DIM_RECTANGLE.getCopy();
    }

    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        g.drawRoundRectangle(r, 20, 20);
        g.translate(getBounds().getLocation());
    }

    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        Rectangle rect = r.getResized(-1, -1);
        g.drawRoundRectangle(rect, 20, 10);
        g.translate(getBounds().getLocation());
    }

    @Override
    public Rectangle getClientArea(Rectangle rect) {
        if (bpmnNotation) {
            return super.getClientArea(rect).getExpanded(-GRID_SIZE / 2, -GRID_SIZE / 2);
        }
        return super.getClientArea(rect);
    }

}

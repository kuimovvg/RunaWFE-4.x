package ru.runa.bpm.ui.common.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import ru.runa.bpm.ui.SharedImages;

public class WaitStateFigure extends StateFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        if (!bpmnNotation) {
            addLabel();
        } else {
            connectionAnchor = new CircleAnchor(this);
        }
    }

    @Override
    public Dimension getDefaultSize() {
        if (bpmnNotation) {
            return DIM_SQUARE.getCopy();
        }
        return DIM_RECTANGLE.getCopy();
    }

    @Override
    public boolean isResizeable() {
        return !bpmnNotation;
    }

    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        g.drawImage(SharedImages.getImage("icons/bpmn/graph/waitstate.png"), r.getLocation());
    }

    @Override
    public Rectangle getClientArea(Rectangle rect) {
        if (bpmnNotation) {
            return getBounds();
        }
        return super.getClientArea(rect);
    }

    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        super.paintUMLFigure(g, r);
        int offset = 5, diameter = 18;
        int center = offset + diameter / 2;
        g.drawOval(offset, offset, diameter, diameter);
        g.drawLine(center, center, center, center + diameter / 2 - 5);
        g.drawLine(center, center, center + diameter / 2 - 5, center - diameter / 2 + 5);
    }

}

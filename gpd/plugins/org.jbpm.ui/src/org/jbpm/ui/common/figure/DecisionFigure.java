package ru.runa.bpm.ui.common.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import ru.runa.bpm.ui.SharedImages;

public class DecisionFigure extends NodeFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        if (!bpmnNotation) {
            addLabel();
        }
        connectionAnchor = new DiamondAnchor(this);
    }

    @Override
    public boolean isResizeable() {
        return !bpmnNotation;
    }

    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        g.drawImage(SharedImages.getImage("icons/bpmn/graph/decision.png"), r.getLocation());
    }

    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        g.translate(getLocation());
        int halfWidth = Math.round(getSize().width / 2);
        int halfHeight = Math.round(getSize().height / 2);
        PointList points = new PointList(4);
        points.addPoint(halfWidth, 0);
        points.addPoint(getSize().width - 1, halfHeight);
        points.addPoint(halfWidth, getSize().height - 1);
        points.addPoint(0, halfHeight);
        g.drawPolygon(points);
    }

}
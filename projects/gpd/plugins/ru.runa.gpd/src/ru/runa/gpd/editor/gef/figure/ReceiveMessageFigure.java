package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.SharedImages;

public class ReceiveMessageFigure extends MessageNodeFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        if (!bpmnNotation) {
            this.connectionAnchor = new ReceiveMessageNodeAnchor(this);
            addEmptySpace(0, 2 * GRID_SIZE);
        }
    }

    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        g.drawImage(SharedImages.getImage("icons/bpmn/graph/receivemessage.png"), r.getLocation());
    }

    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        g.translate(getLocation());
        int halfHeight = Math.round(getSize().height / 2);
        int xLeft = (int) (halfHeight * Math.tan(Math.PI / 6));
        PointList points = new PointList(5);
        points.addPoint(0, 0);
        points.addPoint(getSize().width - 1, 0);
        points.addPoint(getSize().width - 1, getSize().height - 1);
        points.addPoint(0, getSize().height - 1);
        points.addPoint(xLeft, halfHeight);
        g.drawPolygon(points);
    }

    static class ReceiveMessageNodeAnchor extends ChopboxAnchor {

        public ReceiveMessageNodeAnchor(IFigure owner) {
            super(owner);
        }

        @Override
        public Point getLocation(Point reference) {
            Rectangle r = Rectangle.SINGLETON;
            r.setBounds(getOwner().getBounds());
            getOwner().translateToAbsolute(r);
            Point ref = r.getCenter().negate().translate(reference);
            if (ref.x < 0) {
                double cutOffAngle = Math.atan((double) r.height / r.width);
                double refAngle = Math.atan((double) ref.y / ref.x);
                if (Math.abs(refAngle) < cutOffAngle) {
                    double p = (r.width - r.height * Math.tan(Math.PI / 6)) / 2;
                    double k1 = (double) ref.y / ref.x;
                    double b1 = 0;
                    double k2 = r.height / (2 * p - r.width);
                    if (ref.y < 0) {
                        k2 = -1 * k2;
                    }
                    double b2 = k2 * p;
                    double dx = (b2 - b1) / (k1 - k2);
                    double dy = dx * k1 + b1;
                    return new Point(Math.round(r.getCenter().x + dx), Math.round(r.getCenter().y + dy));
                }
            }
            return super.getLocation(reference);
        }
    }

}

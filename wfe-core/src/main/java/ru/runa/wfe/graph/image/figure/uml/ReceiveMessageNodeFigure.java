package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.util.AngleInfo;
import ru.runa.wfe.graph.image.util.DrawProperties;
import ru.runa.wfe.graph.image.util.Line;
import ru.runa.wfe.graph.image.util.LineUtils;

public class ReceiveMessageNodeFigure extends AbstractFigure {

    private Polygon createPolygon() {
        Rectangle r = getRectangle();
        int halfHeight = (int) Math.round(r.getHeight() / 2);
        int xLeft = (int) (r.getMinX() + halfHeight * Math.tan(Math.PI / 6));
        Polygon polygon = new Polygon(new int[] { xLeft, (int) r.getMinX(), (int) r.getMaxX(), (int) r.getMaxX(), (int) r.getMinX() }, new int[] {
                (int) r.getCenterY(), (int) r.getMinY(), (int) r.getMinY(), (int) r.getMaxY(), (int) r.getMaxY() }, 5);
        return polygon;
    }

    @Override
    public void fill(Graphics2D graphics) {
        graphics.fillPolygon(createPolygon());
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        graphics.drawPolygon(createPolygon());
        if (!DrawProperties.useEdgingOnly()) {
            Rectangle r = getRectangle();
            drawTextInfo(graphics, (int) r.getHeight() / 2 - DrawProperties.getFontSize());
        }
    }

    @Override
    public Line createBorderLine(AngleInfo angle) {
        Rectangle r = getRectangle();
        double cutOffAngle = Math.atan((double) r.height / r.width);
        if (angle.getQuarter() == AngleInfo.QUARTER_III && Math.abs(angle.getAngle()) <= cutOffAngle) {
            int p = (int) ((r.width - r.height * Math.tan(Math.PI / 6)) / 2);
            int y = (int) r.getMinY();
            if (angle.getAngle() < 0) {
                y = (int) r.getMaxY();
            }
            return LineUtils.createLine(new Point((int) r.getCenterX() - p, (int) r.getCenterY()), new Point((int) r.getMinX(), y));
        }
        return super.createBorderLine(angle);
    }

}

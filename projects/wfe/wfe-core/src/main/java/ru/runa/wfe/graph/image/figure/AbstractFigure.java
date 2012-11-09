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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.graph.image.GraphImage.RenderHits;
import ru.runa.wfe.graph.image.figure.uml.StateFigure;
import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.graph.image.util.ActionUtils;
import ru.runa.wfe.graph.image.util.AngleInfo;
import ru.runa.wfe.graph.image.util.DrawProperties;
import ru.runa.wfe.graph.image.util.Line;
import ru.runa.wfe.graph.image.util.LineUtils;

public abstract class AbstractFigure {
    private final static Log log = LogFactory.getLog(AbstractFigure.class);

    protected int[] coords;
    protected int type;
    protected String name;
    protected String swimlane;
    protected int actionsCount;
    protected List<Integer> failedActions = new ArrayList<Integer>();

    protected Map<String, TransitionFigure> transitions = new HashMap<String, TransitionFigure>();
    protected RenderHits renderHits;

    public void initFigure(NodeModel model) {
        coords = new int[] { model.getX(), model.getY(), model.getWidth(), model.getHeight() };
        type = model.getType();
        name = model.getName();
        swimlane = model.getSwimlane();
        actionsCount = model.getActionsCount();
    }

    public int[] getCoords() {
        return coords;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public void setRenderHits(RenderHits renderHits) {
        this.renderHits = renderHits;
    }

    public void addFailedAction(int index) {
        failedActions.add(index);
    }

    public Map<String, TransitionFigure> getTransitions() {
        return transitions;
    }

    public void addTransition(String name, TransitionFigure transition) {
        transitions.put(name, transition);
    }

    public TransitionFigure getTransition(String name) {
        return transitions.get(name);
    }

    protected void drawActions(Graphics2D graphics) {
        if (actionsCount > 0) {
            Color color = graphics.getColor();
            if (DrawProperties.useEdgingOnly()) {
                int shiftX = (ActionUtils.ACTION_DELIM + 2) + actionsCount * (ActionUtils.ACTION_SIZE + (ActionUtils.ACTION_DELIM + 3));
                int shiftY = ActionUtils.ACTION_SIZE + 6;
                graphics.setColor(DrawProperties.getBackgroundColor());
                graphics.fillRect(getRectangle().x + getRectangle().width - 4 - shiftX, getRectangle().y + getRectangle().height - 4 - shiftY,
                        shiftX, shiftY);
            }
            for (int i = 0; i < actionsCount; i++) {
                Point loc = ActionUtils.getActionLocationOnNode(i, coords, getClass() == StateFigure.class);
                loc.translate(-1, -1);
                if (failedActions.contains(i)) {
                    graphics.setColor(Color.RED);
                    graphics.drawString("x", loc.x + 3, loc.y + 3);
                }
                graphics.setColor(color);
                graphics.drawOval(loc.x, loc.y, ActionUtils.ACTION_SIZE, ActionUtils.ACTION_SIZE);
            }
        }
    }

    protected void drawTextInfo(Graphics2D graphics, int hOffset) {
        if (!DrawProperties.useEdgingOnly()) {
            Color color = graphics.getColor();
            graphics.setColor(DrawProperties.getTextColor());
            if (swimlane != null) {
                hOffset = drawText(graphics, "(" + swimlane + ")", hOffset);
                // additional space after swimlane label
                hOffset += 3;
            }
            drawText(graphics, name, hOffset);
            graphics.setColor(color);
        }
    }

    private int drawText(Graphics2D graphics, String text, int hOffset) {
        Rectangle r = getTextBoundsRectangle();
        Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(text, graphics);
        if (textBounds.getWidth() > r.getWidth() - 4) {
            int y = coords[1] + hOffset;
            AttributedString attributedString = new AttributedString(text);
            attributedString.addAttribute(TextAttribute.FONT, graphics.getFont());
            AttributedCharacterIterator characterIterator = attributedString.getIterator();
            LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator, graphics.getFontRenderContext());
            while (measurer.getPosition() < characterIterator.getEndIndex()) {
                TextLayout textLayout = measurer.nextLayout((float) r.getWidth() - 4);
                y += textLayout.getAscent();
                float x = (float) (r.getCenterX() + 2 - textLayout.getBounds().getCenterX());
                textLayout.draw(graphics, x, y);
                y += textLayout.getDescent() + textLayout.getLeading();
            }
            return y - coords[1];
        } else {
            graphics.drawString(text, (float) (r.getCenterX() + 2 - textBounds.getCenterX()), (float) (coords[1] + textBounds.getHeight() + hOffset));
            return (int) (textBounds.getHeight() + hOffset + 3);
        }
    }

    protected void drawImage(Graphics2D graphics, String name) {
        try {
            if (!DrawProperties.useEdgingOnly()) {
                BufferedImage image = ImageIO.read(ClassLoaderUtil.getResourceAsStream(name, getClass()));
                graphics.drawRenderedImage(image, AffineTransform.getTranslateInstance(coords[0], coords[1]));
            }
        } catch (IOException e) {
            log.error("Unable to paint image", e);
        }
    }

    public Point getBendpoint() {
        Rectangle allRect = getRectangle();
        return new Point((int) allRect.getCenterX(), (int) allRect.getCenterY());
    }

    public void fill(Graphics2D graphics) {
    }

    public abstract void draw(Graphics2D graphics, boolean cleanMode);

    public Rectangle getRectangle() {
        return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
    }

    public Rectangle getTextBoundsRectangle() {
        return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
    }

    protected AngleInfo getTransitionAngle(double x, double y) {
        Rectangle rect = getRectangle();
        double cx = rect.getCenterX();
        double cy = rect.getCenterY();
        if (x == cx) {
            return (y - cy > 0) ? new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_IV) : new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_II);
        } else {
            double critAngle = rect.getHeight() / rect.getWidth();
            AngleInfo angleInfo = new AngleInfo();
            angleInfo.setAngle((y - cy) / (x - cx));
            if (Math.abs(angleInfo.getAngle()) > critAngle) {
                if (y - cy > 0) {
                    // IV
                    angleInfo.setQuarter(AngleInfo.QUARTER_IV);
                } else {
                    // II
                    angleInfo.setQuarter(AngleInfo.QUARTER_II);
                }
            } else {
                if (x - cx > 0) {
                    // I
                    angleInfo.setQuarter(AngleInfo.QUARTER_I);
                } else {
                    // III
                    angleInfo.setQuarter(AngleInfo.QUARTER_III);
                }
            }
            return angleInfo;
        }
    }

    public Line createBorderLine(AngleInfo angle) {
        Line line = null;
        Rectangle r = getRectangle();

        switch (angle.getQuarter()) {
        case AngleInfo.QUARTER_I:
            line = LineUtils.createLine(new Point((int) r.getMaxX(), (int) r.getMinY()), new Point((int) r.getMaxX(), (int) r.getMaxY()));
            break;
        case AngleInfo.QUARTER_II:
            line = LineUtils.createLine(new Point((int) r.getMinX(), (int) r.getMinY()), new Point((int) r.getMaxX(), (int) r.getMinY()));
            break;
        case AngleInfo.QUARTER_III:
            line = LineUtils.createLine(new Point((int) r.getMinX(), (int) r.getMinY()), new Point((int) r.getMinX(), (int) r.getMaxY()));
            break;
        case AngleInfo.QUARTER_IV:
            line = LineUtils.createLine(new Point((int) r.getMinX(), (int) r.getMaxY()), new Point((int) r.getMaxX(), (int) r.getMaxY()));
            break;
        }
        return line;
    }

    public Point getTransitionPoint(double x, double y, String transitionName) {
        AngleInfo angle = getTransitionAngle(x, y);

        Rectangle r = getRectangle();
        double cx = r.getCenterX();
        double cy = r.getCenterY();

        Line line1 = createBorderLine(angle);
        Line line2 = LineUtils.createLine(new Point((int) cx, (int) cy), angle.getAngle());
        Point intersectionPoint = LineUtils.getIntersectionPoint(line1, line2);
        return intersectionPoint;
    }
}

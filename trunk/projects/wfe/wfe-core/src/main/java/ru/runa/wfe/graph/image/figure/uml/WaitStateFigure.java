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
package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import ru.runa.wfe.graph.image.util.DrawProperties;

public class WaitStateFigure extends TaskNodeFigure {

    @Override
    public Point getTransitionPoint(double x, double y, String transitionName) {
        // time-out-transition treated as from timer in super class
        return super.getTransitionPoint(x, y, null);
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        Rectangle r = getRectangle();
        graphics.drawRoundRect(r.x, r.y, r.width, r.height, 20, 10);
        if (!DrawProperties.useEdgingOnly()) {
            int offset = 5, diameter = 18;
            int center = offset + diameter / 2;
            graphics.drawOval(r.x + offset, r.y + offset, diameter, diameter);
            graphics.drawLine(r.x + center, r.y + center, r.x + center, r.y + center + diameter / 2 - 5);
            graphics.drawLine(r.x + center, r.y + center, r.x + center + diameter / 2 - 5, r.y + center - diameter / 2 + 5);

            int hOffset;
            Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(name, graphics);
            if (textBounds.getWidth() > r.getWidth() - 5) {
                int y = 0;
                AttributedString attributedString = new AttributedString(name);
                attributedString.addAttribute(TextAttribute.FONT, graphics.getFont());
                AttributedCharacterIterator characterIterator = attributedString.getIterator();
                LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator, graphics.getFontRenderContext());
                while (measurer.getPosition() < characterIterator.getEndIndex()) {
                    TextLayout textLayout = measurer.nextLayout((float) (r.getWidth() / 1.4));
                    y += textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();
                }
                hOffset = (int) ((r.getHeight() - y) / 2);
            } else {
                hOffset = (int) (r.getHeight() / 2 - DrawProperties.getFontSize());
            }
            drawTextInfo(graphics, hOffset);
        }
    }

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
    }
}

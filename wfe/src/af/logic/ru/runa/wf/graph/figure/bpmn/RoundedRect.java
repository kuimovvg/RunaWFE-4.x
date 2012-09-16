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
package ru.runa.wf.graph.figure.bpmn;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import ru.runa.wf.graph.figure.AbstractFigure;
import ru.runa.wf.graph.util.DrawProperties;

public class RoundedRect extends AbstractFigure {
    private final String imageName;

    public RoundedRect(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public void fill(Graphics2D graphics) {
        if (imageName == null) {
            Rectangle rect = getRectangle();
            graphics.fillRoundRect(rect.x + 6, rect.y + 6, rect.width - 12, rect.height - 12, 20, 20);
        }
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        if (imageName == null) {
            drawTextInfo(graphics, 1 + DrawProperties.GRID_SIZE / 2);
        } else {
            drawImage(graphics, imageName);
        }
        if (renderHits.isPassed()) {
            Rectangle rect = getRectangle();
            graphics.drawRoundRect(rect.x + 6, rect.y + 6, rect.width - 12, rect.height - 12, 20, 20);
        }
    }

    @Override
    public Rectangle getTextBoundsRectangle() {
        return getRectangle();
    }
}

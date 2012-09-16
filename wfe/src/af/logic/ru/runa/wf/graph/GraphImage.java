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
package ru.runa.wf.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import ru.runa.wf.graph.figure.AbstractFigure;
import ru.runa.wf.graph.figure.TransitionFigure;
import ru.runa.wf.graph.model.DiagramModel;
import ru.runa.wf.graph.util.DrawProperties;

public class GraphImage {
    private static final String FORMAT = "png";

    private BufferedImage origImage = null;

    private final DiagramModel diagramModel;

    private final Map<TransitionFigure, RenderHits> transitions;

    private final Map<AbstractFigure, RenderHits> nodes;

    public GraphImage(byte[] graphBytes, DiagramModel diagramModel, Map<TransitionFigure, RenderHits> transitions,
            Map<AbstractFigure, RenderHits> nodes) throws IOException {
        if (graphBytes != null) {
            origImage = ImageIO.read(new ByteArrayInputStream(graphBytes));
        }
        this.diagramModel = diagramModel;
        this.transitions = transitions;
        this.nodes = nodes;
    }

    public byte[] getImageBytes() throws IOException {
        BufferedImage resultImage = new BufferedImage(diagramModel.getWidth(), diagramModel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resultImage.createGraphics();

        graphics.setFont(new Font(DrawProperties.getFontFamily(), Font.PLAIN, DrawProperties.getFontSize()));
        graphics.setColor(DrawProperties.getBackgroundColor());

        if (origImage != null && DrawProperties.useEdgingOnly()) {
            graphics.drawRenderedImage(origImage, AffineTransform.getRotateInstance(0));
        } else {
            graphics.fillRect(0, 0, diagramModel.getWidth(), diagramModel.getHeight());
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        for (TransitionFigure transitionFigure : transitions.keySet()) {
            RenderHits hits = transitions.get(transitionFigure);
            transitionFigure.setRenderHits(hits);
            transitionFigure.draw(graphics, hits.getColor());
        }
        for (AbstractFigure nodeFigure : nodes.keySet()) {
            RenderHits hits = nodes.get(nodeFigure);
            Stroke stroke;
            if (diagramModel.isUmlNotation()) {
                stroke = new BasicStroke(hits.isActive() ? DrawProperties.FIGURE_SELECTED_BORDER_WIDTH : DrawProperties.FIGURE_BORDER_WIDTH);
            } else {
                stroke = new BasicStroke(DrawProperties.FIGURE_SELECTED_BORDER_WIDTH);
            }
            nodeFigure.setRenderHits(hits);
            drawAbstractFigure(graphics, nodeFigure, hits, stroke);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resultImage, FORMAT, outputStream);
        return outputStream.toByteArray();
    }

    private void drawAbstractFigure(Graphics2D graphics, AbstractFigure figure, RenderHits hits, Stroke stroke) {
        if (DrawProperties.useEdgingOnly()) {
            graphics.setStroke(new BasicStroke(DrawProperties.FIGURE_CLEAN_WIDTH));
            graphics.setColor(DrawProperties.getBackgroundColor());
            figure.draw(graphics, true);
        } else {
            // background
            if (hits.isPassed()) {
                graphics.setColor(hits.isActive() ? DrawProperties.getActiveFigureBackgroundColor() : DrawProperties.getFigureBackgroundColor());
                figure.fill(graphics);
            }
        }

        graphics.setStroke(stroke);
        graphics.setColor(hits.getColor());
        figure.draw(graphics, false);
    }

    public static class RenderHits {
        private final Color color;
        private final boolean active;
        private final boolean passed;

        public RenderHits(Color color) {
            this(color, false, false);
        }

        public RenderHits(Color color, boolean passed) {
            this(color, passed, false);
        }

        public RenderHits(Color color, boolean passed, boolean active) {
            this.color = color;
            this.passed = passed;
            this.active = active;
        }

        public Color getColor() {
            return color;
        }

        public boolean isPassed() {
            return passed;
        }

        public boolean isActive() {
            return active;
        }
    }
}

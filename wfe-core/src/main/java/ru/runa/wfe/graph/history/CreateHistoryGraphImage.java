package ru.runa.wfe.graph.history;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.history.figure.AbstractFigure;
import ru.runa.wfe.graph.history.figure.FiguresNodeData;
import ru.runa.wfe.graph.history.figure.TransitionFigureBase;
import ru.runa.wfe.graph.history.model.DiagramModel;
import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphGenericNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphJoinNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.graph.HistoryGraphNodeVisitor;
import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;

/**
 * Creates image from history graph.
 */
public class CreateHistoryGraphImage implements HistoryGraphNodeVisitor<CreateHistoryGraphImageContext> {

    private static final String FORMAT = "png";
    private final DiagramModel diagramModel;
    private final BufferedImage resultImage;
    private final Graphics2D graphics;

    public CreateHistoryGraphImage(DiagramModel diagramModel) {
        super();
        this.diagramModel = diagramModel;
        resultImage = new BufferedImage(diagramModel.getWidth(), diagramModel.getHeight(), BufferedImage.TYPE_INT_RGB);
        graphics = resultImage.createGraphics();
        graphics.setFont(new Font(DrawProperties.getFontFamily(), Font.PLAIN, DrawProperties.getFontSize()));
        graphics.setColor(DrawProperties.getBackgroundColor());
        graphics.fillRect(0, 0, diagramModel.getWidth(), diagramModel.getHeight());
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    public byte[] getImageBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resultImage, FORMAT, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public void onForkNode(HistoryGraphForkNodeModel node, CreateHistoryGraphImageContext context) {
        commonProcessNode(node, context);
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, CreateHistoryGraphImageContext context) {
        commonProcessNode(node, context);
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, CreateHistoryGraphImageContext context) {
        commonProcessNode(node, context);
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, CreateHistoryGraphImageContext context) {
        commonProcessNode(node, context);
    }

    private void commonProcessNode(HistoryGraphNode node, CreateHistoryGraphImageContext context) {
        FiguresNodeData data = FiguresNodeData.getOrThrow(node);
        drawNode(data);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        drawTransitions(data);
    }

    private void drawTransitions(FiguresNodeData data) {
        for (TransitionFigureBase transition : data.getTransitions()) {
            transition.draw(graphics, transition.getRenderHits().getColor());
        }
    }

    private void drawNode(FiguresNodeData data) {
        RenderHits hits = data.getFigure().getRenderHits();
        int lineWidth = 1;
        if (hits.isActive()) {
            lineWidth *= 2;
        }
        if (!diagramModel.isUmlNotation()) {
            lineWidth *= 2;
        }
        data.getFigure().setRenderHits(hits);
        drawAbstractFigure(graphics, data.getFigure(), hits, new BasicStroke(lineWidth));
    }

    private void drawAbstractFigure(Graphics2D graphics, AbstractFigure figure, RenderHits hits, Stroke stroke) {
        if (hits.isPassed()) {
            graphics.setColor(hits.isActive() ? DrawProperties.getActiveFigureBackgroundColor() : DrawProperties.getFigureBackgroundColor());
            figure.fill(graphics);
        }
        graphics.setStroke(stroke);
        graphics.setColor(hits.getColor());
        figure.draw(graphics, false);
    }
}

package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ITargetContext;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.TextAnnotation;

public class AddTextAnnotationFeature extends AddGraphElementFeature {
    @Override
    public boolean canAdd(IAddContext context) {
        return true;
    }

    @Override
    public Dimension getDefaultSize(ITargetContext context) {
        return new Dimension(100, 50);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        TextAnnotation annotation = (TextAnnotation) context.getNewObject();
        org.eclipse.draw2d.geometry.Rectangle bounds = adjustBounds(context);
        //
        ContainerShape containerShape = Graphiti.getPeCreateService().createContainerShape(context.getTargetContainer(), true);
        Rectangle main = Graphiti.getGaService().createInvisibleRectangle(containerShape);
        Graphiti.getGaService().setLocationAndSize(main, context.getX(), context.getY(), bounds.width, bounds.height);
        int commentEdge = 20;
        final Shape lineShape = Graphiti.getPeCreateService().createShape(containerShape, false);
        final Polyline line = Graphiti.getGaService().createPolyline(lineShape, new int[] { commentEdge, 0, 0, 0, 0, bounds.height, commentEdge, bounds.height });
        line.setStyle(StyleUtil.getStyleForTask(getDiagram()));
        line.setLineWidth(2);
        Graphiti.getGaService().setLocationAndSize(line, 0, 0, commentEdge, bounds.height);
        final Shape textShape = Graphiti.getPeCreateService().createShape(containerShape, false);
        final MultiText text = Graphiti.getGaService().createDefaultMultiText(getDiagram(), textShape, annotation.getDescription());
        //text.setStyle(StyleUtil.getStyleForTask(getDiagram()));
        text.setVerticalAlignment(Orientation.ALIGNMENT_TOP);
        Graphiti.getGaService().setLocationAndSize(text, 5, 5, bounds.width - 5, bounds.height - 5);
        // link both, the container as well as the text shape so direct editing works together
        // with updating and property handling
        link(containerShape, annotation);
        link(textShape, annotation);
        Graphiti.getPeCreateService().createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        return containerShape;
    }
}

package ru.runa.gpd.editor.graphiti.update;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;

import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.TaskState;

public class ResizeNodeFeature extends DefaultResizeShapeFeature {
    public ResizeNodeFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canResizeShape(IResizeShapeContext context) {
        Shape shape = context.getShape();
        Node node = (Node) getBusinessObjectForPictogramElement(shape);
        if (node instanceof TaskState) {
            return true;
        }
        return false;
    }

    @Override
    public void resizeShape(IResizeShapeContext context) {
        super.resizeShape(context);
        Node node = (Node) getBusinessObjectForPictogramElement(context.getShape());
        Rectangle newConstraint = node.getConstraint().getCopy();
        newConstraint.x = context.getX();
        newConstraint.y = context.getY();
        newConstraint.width = context.getWidth();
        newConstraint.height = context.getHeight();
        node.setConstraint(newConstraint);
        //
        int height = context.getHeight();
        int width = context.getWidth();
        context.getShape().getGraphicsAlgorithm().setHeight(height);
        context.getShape().getGraphicsAlgorithm().setWidth(width);
        for (GraphicsAlgorithm graphicsAlgorithm : context.getShape().getGraphicsAlgorithm().getGraphicsAlgorithmChildren()) {
            graphicsAlgorithm.setHeight(height);
            graphicsAlgorithm.setWidth(width);
        }
        List<Shape> shapes = ((ContainerShape) context.getShape()).getChildren();
        for (Shape shape : shapes) {
            if (shape.getGraphicsAlgorithm() != null) {
                if (shape.getGraphicsAlgorithm() instanceof MultiText) {
                    MultiText text = (MultiText) shape.getGraphicsAlgorithm();
                    text.setHeight(height - 25);
                    text.setWidth(width);
                } else if (shape.getGraphicsAlgorithm() instanceof Image) {
                    Image image = (Image) shape.getGraphicsAlgorithm();
                    int imageX = image.getX();
                    if (imageX > 20) {
                        image.setX(width - 20);
                    }
                } else if (shape.getGraphicsAlgorithm() instanceof Text) {
                    Text text = (Text) shape.getGraphicsAlgorithm();
                    text.setHeight(height - 25);
                    text.setWidth(width);
                }
            }
        }
    }
}

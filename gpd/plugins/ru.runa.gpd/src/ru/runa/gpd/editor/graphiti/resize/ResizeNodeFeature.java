package ru.runa.gpd.editor.graphiti.resize;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
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
        node.getConstraint().x = context.getX();
        node.getConstraint().y = context.getY();
        node.getConstraint().width = context.getWidth();
        node.getConstraint().height = context.getHeight();
    }
}

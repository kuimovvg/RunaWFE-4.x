package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;

import ru.runa.gpd.lang.model.Node;

public class MoveNodeFeature extends DefaultMoveShapeFeature {
    public MoveNodeFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canMoveShape(IMoveShapeContext context) {
        return true;
    }

    @Override
    protected void postMoveShape(IMoveShapeContext context) {
        Shape shape = context.getShape();
        Node node = (Node) getBusinessObjectForPictogramElement(shape);
        Rectangle newConstraint = node.getConstraint().getCopy();
        newConstraint.x = context.getX();
        newConstraint.y = context.getY();
        node.setConstraint(newConstraint);
    }
}

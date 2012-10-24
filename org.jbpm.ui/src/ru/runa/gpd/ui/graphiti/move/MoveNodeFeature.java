package ru.runa.gpd.ui.graphiti.move;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.jbpm.ui.common.model.Node;

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
        node.getConstraint().x = context.getX();
        node.getConstraint().y = context.getY();
    }
}

package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;

public class MoveNodeFeature extends DefaultMoveShapeFeature {
    public MoveNodeFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canMoveShape(IMoveShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        if (element instanceof Timer && element.getParent() instanceof ITimed) {
            return false;
        }
        return true;
    }

    @Override
    protected void postMoveShape(IMoveShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        Rectangle newConstraint = element.getConstraint().getCopy();
        newConstraint.x = context.getX();
        newConstraint.y = context.getY();
        element.setConstraint(newConstraint);
    }
}

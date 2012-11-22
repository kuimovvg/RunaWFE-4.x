package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Subprocess;

public abstract class AddNodeFeature extends AbstractAddShapeFeature implements GEFConstants {
    private DiagramFeatureProvider featureProvider;

    public AddNodeFeature() {
        super(null);
    }

    public void setFeatureProvider(DiagramFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    public boolean isFixedSize() {
        return false;
    }

    public abstract Dimension getDefaultSize();

    protected Rectangle adjustBounds(IAddContext context) {
        Rectangle rectangle = new Rectangle(context.getX(), context.getY(), context.getWidth(), context.getHeight());
        Dimension minSize = getDefaultSize();
        if (rectangle.height < minSize.height) {
            rectangle.height = minSize.height;
        }
        if (rectangle.width < minSize.width) {
            rectangle.width = minSize.width;
        }
        ((Node) context.getNewObject()).setConstraint(rectangle);
        return rectangle;
    }

    @Override
    public boolean canAdd(IAddContext context) {
        if (context.getNewObject() instanceof Node) {
            Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
            if (context.getTargetContainer() instanceof Diagram || parentObject instanceof Subprocess) {
                return true;
            }
        }
        return false;
    }
}

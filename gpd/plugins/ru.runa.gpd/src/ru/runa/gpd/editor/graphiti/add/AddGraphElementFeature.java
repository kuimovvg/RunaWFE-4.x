package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ITargetContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class AddGraphElementFeature extends AbstractAddShapeFeature implements GEFConstants {
    private DiagramFeatureProvider featureProvider;

    public AddGraphElementFeature() {
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

    protected ProcessDefinition getProcessDefinition() {
        return (ProcessDefinition) getBusinessObjectForPictogramElement(getDiagram());
    }

    protected Rectangle adjustBounds(IAddContext context) {
        Rectangle rectangle = new Rectangle(context.getX(), context.getY(), context.getWidth(), context.getHeight());
        Dimension minSize = getDefaultSize(context);
        if (rectangle.height < minSize.height) {
            rectangle.height = minSize.height;
        }
        if (rectangle.width < minSize.width) {
            rectangle.width = minSize.width;
        }
        ((GraphElement) context.getNewObject()).setConstraint(rectangle);
        return rectangle;
    }

    public abstract Dimension getDefaultSize(ITargetContext context);
}

package ru.runa.gpd.ui.graphiti.resize;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.jpdl3.model.StartState;

public class ResizeNodeFeature extends DefaultResizeShapeFeature {

    public ResizeNodeFeature(IFeatureProvider provider) {
        super(provider);
    }
    
    @Override
    public boolean canResizeShape(IResizeShapeContext context) {
        Shape shape = context.getShape();
        Node node = (Node) getBusinessObjectForPictogramElement(shape);
        if (node instanceof StartState) {
            return false;
        }
        return false;
        //return super.canResizeShape(context);
    }
}

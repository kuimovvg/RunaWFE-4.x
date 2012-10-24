package ru.runa.gpd.ui.graphiti.delete;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.jbpm.ui.common.model.Node;

public class DeleteNodeFeature extends DefaultDeleteFeature {

    public DeleteNodeFeature(IFeatureProvider provider) {
        super(provider);
    }
    
    @Override
    protected void deleteBusinessObject(Object bo) {
        Node node = (Node) bo;
        node.getParent().removeChild(node);
    }
    
}

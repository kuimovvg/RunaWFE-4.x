package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;

import ru.runa.gpd.lang.model.GraphElement;

public class DeleteElementFeature extends DefaultDeleteFeature {
    public DeleteElementFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected void deleteBusinessObject(Object bo) {
        GraphElement node = (GraphElement) bo;
        node.getParent().removeChild(node);
    }
}

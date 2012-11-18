package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.MultiInstance;

public class CreateMultiInstanceFeature extends AbstractCreateNodeFeature {
    public CreateMultiInstanceFeature(DiagramFeatureProvider provider) {
        super(provider, MultiInstance.class);
    }
}

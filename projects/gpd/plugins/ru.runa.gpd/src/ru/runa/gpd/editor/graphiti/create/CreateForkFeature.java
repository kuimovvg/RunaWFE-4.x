package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.Fork;

public class CreateForkFeature extends AbstractCreateNodeFeature {
    public CreateForkFeature(DiagramFeatureProvider provider) {
        super(provider, Fork.class);
    }
}

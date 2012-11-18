package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.EndState;

public class CreateEndEventFeature extends AbstractCreateNodeFeature {
    public CreateEndEventFeature(DiagramFeatureProvider provider) {
        super(provider, EndState.class);
    }
}

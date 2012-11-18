package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.Join;

public class CreateJoinFeature extends AbstractCreateNodeFeature {
    public CreateJoinFeature(DiagramFeatureProvider provider) {
        super(provider, Join.class);
    }
}

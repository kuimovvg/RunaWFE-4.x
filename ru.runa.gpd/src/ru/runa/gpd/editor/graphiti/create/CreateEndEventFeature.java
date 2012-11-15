package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;

public class CreateEndEventFeature extends AbstractCreateNodeFeature {
    public static final String ID = "end-state";

    public CreateEndEventFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }
}

package ru.runa.gpd.ui.graphiti.create;

import ru.runa.gpd.ui.graphiti.DiagramFeatureProvider;

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

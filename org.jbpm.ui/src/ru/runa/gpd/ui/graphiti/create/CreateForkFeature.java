package ru.runa.gpd.ui.graphiti.create;

import ru.runa.gpd.ui.graphiti.DiagramFeatureProvider;

public class CreateForkFeature extends AbstractCreateNodeFeature {
    public static final String ID = "fork";

    public CreateForkFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }

}

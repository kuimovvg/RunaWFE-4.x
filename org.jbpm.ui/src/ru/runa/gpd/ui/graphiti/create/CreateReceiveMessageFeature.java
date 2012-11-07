package ru.runa.gpd.ui.graphiti.create;

import ru.runa.gpd.ui.graphiti.DiagramFeatureProvider;

public class CreateReceiveMessageFeature extends AbstractCreateNodeFeature {
    public static final String ID = "receive-message";

    public CreateReceiveMessageFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }

}

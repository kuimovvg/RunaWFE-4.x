package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;

public class CreateSendMessageFeature extends AbstractCreateNodeFeature {
    public static final String ID = "send-message";

    public CreateSendMessageFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }

}

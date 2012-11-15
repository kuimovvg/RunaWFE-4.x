package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;

public class CreateJoinFeature extends AbstractCreateNodeFeature {
    public static final String ID = "join";

    public CreateJoinFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }

}

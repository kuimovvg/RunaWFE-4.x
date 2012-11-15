package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;

public class CreateSubProcessFeature extends AbstractCreateNodeFeature {
    public static final String ID = "process-state";

    public CreateSubProcessFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }

}

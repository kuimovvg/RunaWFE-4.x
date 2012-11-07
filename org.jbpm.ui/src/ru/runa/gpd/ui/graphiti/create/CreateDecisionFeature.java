package ru.runa.gpd.ui.graphiti.create;

import ru.runa.gpd.ui.graphiti.DiagramFeatureProvider;

public class CreateDecisionFeature extends AbstractCreateNodeFeature {
    public static final String ID = "decision";

    public CreateDecisionFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }

}

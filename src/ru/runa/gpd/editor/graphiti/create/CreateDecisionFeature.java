package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.Decision;

public class CreateDecisionFeature extends AbstractCreateNodeFeature {
    public CreateDecisionFeature(DiagramFeatureProvider provider) {
        super(provider, Decision.class);
    }
}

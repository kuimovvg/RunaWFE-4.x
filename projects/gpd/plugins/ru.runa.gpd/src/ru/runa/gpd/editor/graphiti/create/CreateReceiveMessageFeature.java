package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.ReceiveMessageNode;

public class CreateReceiveMessageFeature extends AbstractCreateNodeFeature {
    public CreateReceiveMessageFeature(DiagramFeatureProvider provider) {
        super(provider, ReceiveMessageNode.class);
    }
}

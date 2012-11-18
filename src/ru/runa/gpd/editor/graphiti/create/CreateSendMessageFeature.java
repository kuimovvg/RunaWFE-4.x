package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.SendMessageNode;

public class CreateSendMessageFeature extends AbstractCreateNodeFeature {
    public CreateSendMessageFeature(DiagramFeatureProvider provider) {
        super(provider, SendMessageNode.class);
    }
}

package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.Subprocess;

public class CreateSubProcessFeature extends AbstractCreateNodeFeature {
    public CreateSubProcessFeature(DiagramFeatureProvider provider) {
        super(provider, Subprocess.class);
    }
}

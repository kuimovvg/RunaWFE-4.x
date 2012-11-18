package ru.runa.gpd.editor.graphiti.create;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.TaskState;

public class CreateTaskStateFeature extends AbstractCreateNodeFeature {
    public CreateTaskStateFeature(DiagramFeatureProvider provider) {
        super(provider, TaskState.class);
    }
}

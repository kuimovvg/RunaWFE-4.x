package ru.runa.gpd.ui.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;
import org.jbpm.ui.common.model.Subprocess;
import org.jbpm.ui.jpdl3.model.StartState;

import ru.runa.gpd.ui.graphiti.DiagramFeatureProvider;

public class CreateTaskStateFeature extends AbstractCreateNodeFeature {
    public static final String ID = "task-node";

    public CreateTaskStateFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }

}

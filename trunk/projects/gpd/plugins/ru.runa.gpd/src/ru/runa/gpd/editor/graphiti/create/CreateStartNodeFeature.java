package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;

public class CreateStartNodeFeature extends AbstractCreateNodeFeature {
    public static final String ID = "start-state";

    public CreateStartNodeFeature(DiagramFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected String getNodeId() {
        return ID;
    }

    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        if (parentObject instanceof Subprocess) {
            return false;
        }
        if (getProcessDefinition().getChildren(StartState.class).size() > 0) {
            return false;
        }
        return super.canCreate(context);
    }
}

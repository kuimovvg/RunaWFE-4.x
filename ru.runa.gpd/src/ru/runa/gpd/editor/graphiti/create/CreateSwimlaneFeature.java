package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.lang.model.ProcessDefinition;

public class CreateSwimlaneFeature extends CreateElementFeature {
    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        return parentObject instanceof ProcessDefinition;
    }
}

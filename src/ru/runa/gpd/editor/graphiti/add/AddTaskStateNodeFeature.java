package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.TaskState;

public class AddTaskStateNodeFeature extends AddStateNodeFeature {

    @Override
    public PictogramElement add(IAddContext context) {
        TaskState taskState = (TaskState) context.getNewObject();
        PictogramElement pe = super.add(context);
        pe.getProperties().add(new GaProperty(GaProperty.MINIMAZED_VIEW, String.valueOf(taskState.isMinimizedView())));
        return pe;
    }

}

package ru.runa.gpd.editor.graphiti.layout;

import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.TaskState;

public class LayoutTaskStateFeature extends LayoutStateNodeFeature {

    @Override
    public boolean layout(ILayoutContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();
        TaskState taskState = (TaskState) getBusinessObjectForPictogramElement(containerShape);
        if (taskState.isMinimizedView()) {
            GraphicsAlgorithm mainRectangle = PropertyUtil.findGaRecursiveByName(ga, MAIN_RECT);
            Graphiti.getGaService().setSize(mainRectangle, 3 * GRID_SIZE, 3 * GRID_SIZE);
            GraphicsAlgorithm borderRectangle = PropertyUtil.findGaRecursiveByName(ga, BORDER_RECT);
            Graphiti.getGaService().setLocationAndSize(borderRectangle, 0, 0, 3 * GRID_SIZE, 3 * GRID_SIZE);
            GraphicsAlgorithm swimlaneText = PropertyUtil.findGaRecursiveByName(ga, GaProperty.SWIMLANE_NAME);
            if (swimlaneText != null) {
                Graphiti.getGaService().setLocationAndSize(swimlaneText, 0, 0, 0, 0);
            }
            GraphicsAlgorithm nameText = PropertyUtil.findGaRecursiveByName(ga, GaProperty.NAME);
            Graphiti.getGaService().setLocationAndSize(nameText, 0, 0, 0, 0);
            return true;
        } else {
            return super.layout(context);
        }
    }
}

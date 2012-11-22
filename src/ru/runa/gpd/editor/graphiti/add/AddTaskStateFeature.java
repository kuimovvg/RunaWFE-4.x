package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.State;

public class AddTaskStateFeature extends AddStateNodeFeature {
    @Override
    protected void drawCustom(IAddContext context, GraphicsAlgorithmContainer container) {
        State state = (State) context.getNewObject();
        org.eclipse.draw2d.geometry.Rectangle bounds = state.getConstraint();
        if (state.timerExist()) {
            IPeCreateService createService = Graphiti.getPeCreateService();
            IGaService gaService = Graphiti.getGaService();
            ContainerShape timerShape = createService.createContainerShape(context.getTargetContainer(), true);
            Ellipse timerEllipse = gaService.createEllipse(timerShape);
            timerEllipse.setForeground(gaService.manageColor(getDiagram(), StyleUtil.LIGHT_BLUE));
            timerEllipse.setBackground(gaService.manageColor(getDiagram(), StyleUtil.VERY_LIGHT_BLUE));
            timerEllipse.setFilled(true);
            timerEllipse.setLineWidth(2);
            gaService.setLocationAndSize(timerEllipse, bounds.x - GRID_SIZE, bounds.y + bounds.height - GRID_SIZE, GRID_SIZE * 2, GRID_SIZE * 2);
            //
            Polyline l = gaService.createPolyline(timerEllipse, new int[] { GRID_SIZE, GRID_SIZE + 5, GRID_SIZE, GRID_SIZE, GRID_SIZE + 7, GRID_SIZE - 7 });
            l.setForeground(gaService.manageColor(getDiagram(), StyleUtil.LIGHT_BLUE));
            l.setLineWidth(2);
        }
    }
}

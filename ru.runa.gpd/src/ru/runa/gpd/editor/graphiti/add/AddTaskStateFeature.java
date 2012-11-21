package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Color;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.State;

public class AddTaskStateFeature extends AddNodeFeature {
    @Override
    public Dimension getDefaultSize() {
        return new Dimension(10 * GRID_SIZE, 6 * GRID_SIZE);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        State node = (State) context.getNewObject();
        org.eclipse.draw2d.geometry.Rectangle bounds = getFigureBounds(context);
        //
        IPeCreateService createService = Graphiti.getPeCreateService();
        ContainerShape containerShape = createService.createContainerShape(context.getTargetContainer(), true);
        IGaService gaService = Graphiti.getGaService();
        Rectangle main = gaService.createInvisibleRectangle(containerShape);
        gaService.setLocationAndSize(main, context.getX(), context.getY(), bounds.width, bounds.height);
        //
        int borderWidth = bounds.width - GRID_SIZE;
        int borderHeight = bounds.height - GRID_SIZE;
        RoundedRectangle border = gaService.createRoundedRectangle(main, 20, 20);
        border.setLineWidth(2);
        Color veryLightBlue = gaService.manageColor(getDiagram(), 246, 247, 255);
        Color lightBlue = gaService.manageColor(getDiagram(), 3, 104, 154);
        border.setForeground(lightBlue);
        border.setBackground(veryLightBlue);
        border.setStyle(StyleUtil.getStyleForEvent(getDiagram()));
        gaService.setLocationAndSize(border, GRID_SIZE / 2, GRID_SIZE / 2, borderWidth, borderHeight);
        //
        Text swimlaneText = gaService.createDefaultText(getDiagram(), border, node.getSwimlaneLabel());
        swimlaneText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        gaService.setLocationAndSize(swimlaneText, 0, 0, borderWidth, 2 * GRID_SIZE);
        //
        Text nameText = gaService.createDefaultText(getDiagram(), border, node.getName());
        nameText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        gaService.setLocationAndSize(nameText, 0, 2 * GRID_SIZE, borderWidth, borderHeight - 3 * GRID_SIZE);
        //
        if (!node.timerExist()) {
            ContainerShape timerShape = createService.createContainerShape(context.getTargetContainer(), true);
            Ellipse timerEllipse = gaService.createEllipse(timerShape);
            timerEllipse.setForeground(lightBlue);
            timerEllipse.setBackground(veryLightBlue);
            timerEllipse.setFilled(true);
            timerEllipse.setLineWidth(2);
            gaService.setLocationAndSize(timerEllipse, context.getX() - GRID_SIZE, context.getY() + context.getHeight() - GRID_SIZE, GRID_SIZE * 2, GRID_SIZE * 2);
            //
            Polyline l = gaService.createPolyline(timerEllipse, new int[] { GRID_SIZE, GRID_SIZE + 5, GRID_SIZE, GRID_SIZE, GRID_SIZE + 7, GRID_SIZE - 7 });
            l.setForeground(lightBlue);
            l.setLineWidth(2);
        }
        link(containerShape, node);
        //
        createService.createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        node.setConstraint(bounds);
        return containerShape;
    }
}

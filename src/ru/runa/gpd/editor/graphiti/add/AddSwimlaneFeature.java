package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ITargetContext;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.util.SwimlaneDisplayMode;

public class AddSwimlaneFeature extends AddGraphElementFeature {
    @Override
    public boolean canAdd(IAddContext context) {
        if (context.getNewObject() instanceof Swimlane) {
            Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
            return parentObject instanceof ProcessDefinition;
        }
        return false;
    }

    @Override
    public Dimension getDefaultSize(ITargetContext context) {
        Dimension horizontal = new Dimension(500, 150);
        if (getProcessDefinition().getSwimlaneDisplayMode() == SwimlaneDisplayMode.vertical) {
            horizontal.negate();
        }
        return horizontal;
    }

    @Override
    public PictogramElement add(IAddContext context) {
        Swimlane swimlane = (Swimlane) context.getNewObject();
        org.eclipse.draw2d.geometry.Rectangle bounds = adjustBounds(context);
        org.eclipse.draw2d.geometry.Rectangle nameBounds = bounds.getCopy();
        if (getProcessDefinition().getSwimlaneDisplayMode() == SwimlaneDisplayMode.vertical) {
            nameBounds.setHeight(2 * GRID_SIZE);
        } else {
            nameBounds.setWidth(2 * GRID_SIZE);
        }
        //
        IPeCreateService createService = Graphiti.getPeCreateService();
        ContainerShape nodeShape = createService.createContainerShape(context.getTargetContainer(), true);
        IGaService gaService = Graphiti.getGaService();
        Rectangle main = gaService.createRectangle(nodeShape);
        main.setForeground(gaService.manageColor(getDiagram(), StyleUtil.LIGHT_BLUE));
        main.setBackground(gaService.manageColor(getDiagram(), StyleUtil.VERY_LIGHT_BLUE));
        main.setStyle(StyleUtil.getStyleForEvent(getDiagram()));
        main.setLineWidth(1);
        gaService.setLocationAndSize(main, bounds.x, bounds.y, bounds.width, bounds.height);
        //
        Rectangle nameRectangle = gaService.createRectangle(main);
        nameRectangle.setForeground(gaService.manageColor(getDiagram(), StyleUtil.LIGHT_BLUE));
        nameRectangle.setBackground(gaService.manageColor(getDiagram(), StyleUtil.VERY_LIGHT_BLUE));
        nameRectangle.setStyle(StyleUtil.getStyleForEvent(getDiagram()));
        gaService.setLocationAndSize(nameRectangle, 0, 0, nameBounds.width, nameBounds.height);
        Text nameText = gaService.createDefaultText(getDiagram(), nameRectangle, swimlane.getName());
        nameText.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.NAME));
        nameText.setAngle(90);
        nameText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        nameText.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
        gaService.setLocationAndSize(nameText, 0, 0, nameBounds.width, nameBounds.height);
        // 
        link(nodeShape, swimlane);
        //
        createService.createChopboxAnchor(nodeShape);
        layoutPictogramElement(nodeShape);
        return nodeShape;
    }
}

package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.lang.model.Node;

public class AddNodeWithImageFeature extends AddNodeFeature {
    @Override
    public boolean isFixedSize() {
        return true;
    }

    @Override
    public Dimension getDefaultSize() {
        return new Dimension(4 * GRID_SIZE, 4 * GRID_SIZE);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        Node node = (Node) context.getNewObject();
        Rectangle bounds = getFigureBounds(context);
        ContainerShape parent = context.getTargetContainer();
        IPeCreateService createService = Graphiti.getPeCreateService();
        ContainerShape containerShape = createService.createContainerShape(parent, true);
        IGaService gaService = Graphiti.getGaService();
        Image image = gaService.createImage(containerShape, "graph/" + node.getTypeDefinition().getIcon());
        gaService.setLocationAndSize(image, bounds.x, bounds.y, bounds.width, bounds.height);
        link(containerShape, node);
        createService.createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        node.setConstraint(bounds);
        return containerShape;
    }
}

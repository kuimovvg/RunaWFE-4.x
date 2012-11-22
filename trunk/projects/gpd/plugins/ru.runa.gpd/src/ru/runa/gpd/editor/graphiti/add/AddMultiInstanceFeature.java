package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.MultiInstance;
import ru.runa.gpd.lang.model.PropertyNames;

public class AddMultiInstanceFeature extends AddStateNodeFeature {
    @Override
    protected void drawCustom(IAddContext context, GraphicsAlgorithmContainer container) {
        MultiInstance multiInstance = (MultiInstance) context.getNewObject();
        org.eclipse.draw2d.geometry.Rectangle bounds = multiInstance.getConstraint();
        IGaService gaService = Graphiti.getGaService();
        Image image = gaService.createImage(container, "graph/multiinstance.png");
        image.getProperties().add(new GaProperty(PropertyNames.PROPERTY_MULTIPROCESS, multiInstance.getSubProcessName()));
        gaService.setLocationAndSize(image, bounds.width / 2 - 8, bounds.height - 2 * GRID_SIZE, 16, 12);
    }
}

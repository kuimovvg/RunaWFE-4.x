package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.MultiSubprocess;

public class AddMultiInstanceFeature extends AddStateNodeFeature {
    @Override
    protected void drawCustom(IAddContext context, GraphicsAlgorithmContainer container) {
        MultiSubprocess multiInstance = (MultiSubprocess) context.getNewObject();
        org.eclipse.draw2d.geometry.Rectangle bounds = multiInstance.getConstraint();
        IGaService gaService = Graphiti.getGaService();
        Image image = gaService.createImage(container, "graph/multiinstance.png");
        image.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.MULTIPROCESS));
        gaService.setLocationAndSize(image, bounds.width / 2 - 8, bounds.height - 2 * GRID_SIZE, 16, 12);
    }
}

package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.Subprocess;

public class AddSubProcessFeature extends AddStateNodeFeature {
    @Override
    protected void drawCustom(IAddContext context, GraphicsAlgorithmContainer container) {
        Subprocess subprocess = (Subprocess) context.getNewObject();
        org.eclipse.draw2d.geometry.Rectangle bounds = subprocess.getConstraint();
        IGaService gaService = Graphiti.getGaService();
        Image image = gaService.createImage(container, "graph/subprocess.png");
        image.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.SUBPROCESS));
        gaService.setLocationAndSize(image, bounds.width / 2 - 7, bounds.height - 2 * GRID_SIZE, 14, 14);
    }
}

package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.StartState;

public class AddStartNodeFeature extends AddNodeFeature {
    @Override
    public PictogramElement add(IAddContext context) {
        StartState startState = (StartState) context.getNewObject();
        Dimension bounds = adjustBounds(context);
        ContainerShape containerShape = Graphiti.getPeCreateService().createContainerShape(context.getTargetContainer(), true);
        Rectangle main = Graphiti.getGaService().createInvisibleRectangle(containerShape);
        Graphiti.getGaService().setLocationAndSize(main, context.getX(), context.getY(), bounds.width, bounds.height);
        //
        Shape textShape = Graphiti.getPeCreateService().createShape(containerShape, false);
        Text text = Graphiti.getGaService().createDefaultText(getDiagram(), textShape, startState.getSwimlaneLabel());
        text.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.SWIMLANE_NAME));
        text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        IDimension textDimension = GraphitiUi.getUiLayoutService().calculateTextSize(startState.getSwimlaneLabel(), text.getFont());
        Graphiti.getGaService().setLocationAndSize(text, (bounds.width - textDimension.getWidth()) / 2, 0, textDimension.getWidth(), textDimension.getHeight());
        //
        Dimension imageBounds = new Dimension(4 * GRID_SIZE, 4 * GRID_SIZE);
        Shape imageShape = Graphiti.getPeCreateService().createShape(containerShape, true);
        Image image = Graphiti.getGaService().createImage(imageShape, "graph/" + startState.getTypeDefinition().getIcon());
        Graphiti.getGaService().setLocationAndSize(image, (bounds.width - imageBounds.width) / 2, bounds.height - imageBounds.height, imageBounds.width, imageBounds.height);
        //
        link(containerShape, startState);
        //link(imageShape, startState);
        Graphiti.getPeCreateService().createChopboxAnchor(imageShape);
        layoutPictogramElement(containerShape);
        return containerShape;
    }
}

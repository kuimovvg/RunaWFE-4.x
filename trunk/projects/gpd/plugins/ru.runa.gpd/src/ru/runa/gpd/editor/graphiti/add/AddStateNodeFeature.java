package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.SwimlanedNode;

public class AddStateNodeFeature extends AddNodeFeature {
    @Override
    public Dimension getDefaultSize() {
        return new Dimension(11 * GRID_SIZE, 7 * GRID_SIZE);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        Node node = (Node) context.getNewObject();
        org.eclipse.draw2d.geometry.Rectangle bounds = adjustBounds(context);
        //
        IPeCreateService createService = Graphiti.getPeCreateService();
        ContainerShape nodeShape = createService.createContainerShape(context.getTargetContainer(), true);
        IGaService gaService = Graphiti.getGaService();
        Rectangle main = gaService.createInvisibleRectangle(nodeShape);
        gaService.setLocationAndSize(main, bounds.x, bounds.y, bounds.width, bounds.height);
        //
        int borderWidth = bounds.width - GRID_SIZE;
        int borderHeight = bounds.height - GRID_SIZE;
        RoundedRectangle border = gaService.createRoundedRectangle(main, 20, 20);
        border.setLineWidth(2);
        border.setForeground(gaService.manageColor(getDiagram(), StyleUtil.LIGHT_BLUE));
        border.setBackground(gaService.manageColor(getDiagram(), StyleUtil.VERY_LIGHT_BLUE));
        border.setStyle(StyleUtil.getStyleForEvent(getDiagram()));
        gaService.setLocationAndSize(border, GRID_SIZE / 2, GRID_SIZE / 2, borderWidth, borderHeight);
        if (node instanceof SwimlanedNode) {
            Text swimlaneText = gaService.createDefaultText(getDiagram(), border, ((SwimlanedNode) node).getSwimlaneLabel());
            swimlaneText.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.SWIMLANE_NAME));
            swimlaneText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
            gaService.setLocationAndSize(swimlaneText, 0, 0, borderWidth, 2 * GRID_SIZE);
        }
        MultiText nameText = gaService.createDefaultMultiText(getDiagram(), border, node.getName());
        nameText.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.NAME));
        nameText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        //gaService.setLocation(nameText, 0, 2 * GRID_SIZE);
        //TextUtil.setTextSize(nameText, node.getName(), borderWidth);
        gaService.setLocationAndSize(nameText, 0, 2 * GRID_SIZE, borderWidth, borderHeight - 4 * GRID_SIZE);
        //
        drawCustom(context, main);
        // 
        link(nodeShape, node);
        //
        createService.createChopboxAnchor(nodeShape);
        layoutPictogramElement(nodeShape);
        return nodeShape;
    }

    protected void drawCustom(IAddContext context, GraphicsAlgorithmContainer container) {
    }
}

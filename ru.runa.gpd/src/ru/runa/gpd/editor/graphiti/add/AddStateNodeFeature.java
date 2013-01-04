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

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.editor.graphiti.layout.LayoutStateNodeFeature;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.util.SwimlaneDisplayMode;

public class AddStateNodeFeature extends AddNodeFeature {
    @Override
    public PictogramElement add(IAddContext context) {
        Node node = (Node) context.getNewObject();
        Dimension bounds = adjustBounds(context);
        //
        ContainerShape containerShape = Graphiti.getPeCreateService().createContainerShape(context.getTargetContainer(), true);
        Rectangle main = Graphiti.getGaService().createInvisibleRectangle(containerShape);
        Graphiti.getGaService().setLocationAndSize(main, context.getX(), context.getY(), bounds.width, bounds.height);
        //
        RoundedRectangle border = Graphiti.getGaService().createRoundedRectangle(main, 20, 20);
        border.getProperties().add(new GaProperty(GaProperty.ID, LayoutStateNodeFeature.BORDER_RECT));
        border.setLineWidth(2);
        border.setForeground(Graphiti.getGaService().manageColor(getDiagram(), StyleUtil.LIGHT_BLUE));
        border.setBackground(Graphiti.getGaService().manageColor(getDiagram(), StyleUtil.VERY_LIGHT_BLUE));
        border.setStyle(StyleUtil.getStyleForEvent(getDiagram()));
        if (node instanceof SwimlanedNode && node.getProcessDefinition().getSwimlaneDisplayMode() == SwimlaneDisplayMode.none) {
            Text swimlaneText = Graphiti.getGaService().createDefaultText(getDiagram(), border, ((SwimlanedNode) node).getSwimlaneLabel());
            swimlaneText.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.SWIMLANE_NAME));
            swimlaneText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        }
        MultiText nameText = Graphiti.getGaService().createDefaultMultiText(getDiagram(), border, node.getName());
        nameText.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.NAME));
        nameText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        //
        addCustomGraphics(node, context, main);
        // 
        link(containerShape, node);
        //
        Graphiti.getPeCreateService().createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        return containerShape;
    }

    protected void addCustomGraphics(Node node, IAddContext context, GraphicsAlgorithmContainer container) {
    }
}

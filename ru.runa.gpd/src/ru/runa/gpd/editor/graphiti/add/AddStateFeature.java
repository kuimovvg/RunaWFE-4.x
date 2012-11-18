package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Color;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.State;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;

public class AddStateFeature extends AbstractAddShapeFeature {
    public AddStateFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        final int GRID_SIZE = 12;
        State node = (State) context.getNewObject();
        boolean timerExist = node.timerExist();
        ContainerShape parent = context.getTargetContainer();
        IPeCreateService createService = Graphiti.getPeCreateService();
        ContainerShape containerShape = createService.createContainerShape(parent, true);
        IGaService gaService = Graphiti.getGaService();
        Rectangle invisible = gaService.createInvisibleRectangle(containerShape);
        int width = context.getWidth() <= 10 * GRID_SIZE ? 10 * GRID_SIZE : context.getWidth();
        int height = context.getHeight() <= 6 * GRID_SIZE ? 6 * GRID_SIZE : context.getHeight();
        RoundedRectangle circle;
        {
            circle = gaService.createRoundedRectangle(invisible, 20, 20);
            circle.setLineWidth(2);
            Color veryLightBlue = gaService.manageColor(getDiagram(), 246, 247, 255);
            Color lightBlue = gaService.manageColor(getDiagram(), 3, 104, 154);
            circle.setForeground(lightBlue);
            circle.setBackground(veryLightBlue);
            Swimlane swimlane = node.getSwimlane();
            String swimlaneString = swimlane != null ? "(" + swimlane.getName() + ")" : "";
            Text st = gaService.createDefaultText(getDiagram(), circle, swimlaneString);
            st.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
            gaService.setLocationAndSize(st, 0, 0, width, height / 2);
            Text t = gaService.createDefaultText(getDiagram(), circle, node.getName()); // node.getDescription()
            t.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
            gaService.setLocationAndSize(t, 0, height / 2, width, height / 2);
            gaService.setLocationAndSize(invisible, context.getX() - width / 2 - GRID_SIZE, context.getY() - height / 2 - GRID_SIZE, width + GRID_SIZE * 2, height + GRID_SIZE * 2);
            gaService.setLocationAndSize(circle, GRID_SIZE, GRID_SIZE, width, height);
            circle.setStyle(StyleUtil.getStyleForEvent(getDiagram()));
            if (timerExist) {
                {
                    Ellipse e = gaService.createEllipse(invisible);
                    e.setForeground(lightBlue);
                    e.setBackground(veryLightBlue);
                    e.setFilled(true);
                    e.setLineWidth(2);
                    gaService.setLocationAndSize(e, 2, height - 2, GRID_SIZE * 2, GRID_SIZE * 2);
                }
                {
                    Polyline l = gaService.createPolyline(invisible, new int[] { GRID_SIZE + 2, GRID_SIZE + height + 3, GRID_SIZE + 2, GRID_SIZE + height - 2, GRID_SIZE + 7,
                            GRID_SIZE + height - 7 });
                    l.setForeground(lightBlue);
                    l.setLineWidth(2);
                }
            }
            link(containerShape, node);
        }
        createService.createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        node.setConstraint(new org.eclipse.draw2d.geometry.Rectangle(context.getX(), context.getY(), width, height));
        return containerShape;
    }

    @Override
    public boolean canAdd(IAddContext context) {
        if (context.getNewObject() instanceof Node) {
            Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
            if (context.getTargetContainer() instanceof Diagram || parentObject instanceof Subprocess) {
                return true;
            }
        }
        return false;
    }
}

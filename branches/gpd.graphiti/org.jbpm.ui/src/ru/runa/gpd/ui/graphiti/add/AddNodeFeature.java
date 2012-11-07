package ru.runa.gpd.ui.graphiti.add;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.jbpm.ui.common.model.EndState;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.Subprocess;

import ru.runa.gpd.ui.graphiti.StyleUtil;

public class AddNodeFeature extends AbstractAddShapeFeature {
    public AddNodeFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        Node node = (Node) context.getNewObject();
        ContainerShape parent = context.getTargetContainer();
        IPeCreateService createService = Graphiti.getPeCreateService();
        ContainerShape containerShape = createService.createContainerShape(parent, true);
        int width = context.getWidth() <= 35 ? 35 : context.getWidth();
        int height = context.getHeight() <= 35 ? 35 : context.getHeight();
        IGaService gaService = Graphiti.getGaService();
        Ellipse circle;
        {
            Ellipse invisibleCircle = gaService.createEllipse(containerShape);
            invisibleCircle.setFilled(false);
            invisibleCircle.setLineVisible(false);
            gaService.setLocationAndSize(invisibleCircle, context.getX()-width/2, context.getY()-height/2, width, height);
            circle = gaService.createEllipse(invisibleCircle);
            circle.setParentGraphicsAlgorithm(invisibleCircle);
            circle.setStyle(StyleUtil.getStyleForEvent(getDiagram()));
            if (node instanceof EndState) {
                circle.setLineWidth(3);
            }
            gaService.setLocationAndSize(circle, 0, 0, width, height);
            link(containerShape, node);
        }
        // add a chopbox anchor to the shape
        createService.createChopboxAnchor(containerShape);
        //        if (!(addedEvent instanceof EndEvent)) {
        //            // create an additional box relative anchor at middle-right
        //            final BoxRelativeAnchor boxAnchor = peCreateService.createBoxRelativeAnchor(containerShape);
        //            boxAnchor.setRelativeWidth(1.0);
        //            boxAnchor.setRelativeHeight(0.51);
        //            boxAnchor.setReferencedGraphicsAlgorithm(circle);
        //            final Ellipse ellipse = ActivitiUiUtil.createInvisibleEllipse(boxAnchor, gaService);
        //            gaService.setLocationAndSize(ellipse, 0, 0, 0, 0);
        //        }
        //        if (addedEvent instanceof StartEvent && ((StartEvent) addedEvent).getEventDefinitions().size() > 0) {
        //            StartEvent startEvent = (StartEvent) addedEvent;
        //            final Shape shape = peCreateService.createShape(containerShape, false);
        //            Image image = null;
        //            if (startEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
        //                image = gaService.createImage(shape, PluginImage.IMG_BOUNDARY_TIMER.getImageKey());
        //            } else if (startEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
        //                image = gaService.createImage(shape, PluginImage.IMG_STARTEVENT_MESSAGE.getImageKey());
        //            } else
        //                image = gaService.createImage(shape, PluginImage.IMG_BOUNDARY_ERROR.getImageKey());
        //            image.setWidth(IMAGE_SIZE);
        //            image.setHeight(IMAGE_SIZE);
        //            gaService.setLocationAndSize(image, (width - IMAGE_SIZE) / 2, (height - IMAGE_SIZE) / 2, IMAGE_SIZE, IMAGE_SIZE);
        //        }
        layoutPictogramElement(containerShape);
        node.setConstraint(new Rectangle(context.getX(), context.getY(), context.getWidth(), context.getHeight()));
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

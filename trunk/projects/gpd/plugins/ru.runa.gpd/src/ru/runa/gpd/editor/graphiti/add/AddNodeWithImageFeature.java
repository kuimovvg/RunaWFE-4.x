package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Fork;
import ru.runa.gpd.lang.model.Join;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ReceiveMessageNode;
import ru.runa.gpd.lang.model.SendMessageNode;
import ru.runa.gpd.lang.model.StartState;

public class AddNodeWithImageFeature extends AbstractAddNodeFeature {
    public AddNodeWithImageFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean isFixedSize() {
        return true;
    }

    @Override
    public PictogramElement add(IAddContext context) {
        Node node = (Node) context.getNewObject();
        ContainerShape parent = context.getTargetContainer();
        IPeCreateService createService = Graphiti.getPeCreateService();
        ContainerShape containerShape = createService.createContainerShape(parent, true);
        IGaService gaService = Graphiti.getGaService();
        Image image = null;
        if (node instanceof StartState) {
            image = gaService.createImage(containerShape, "../graph/start.png");
        } else if (node instanceof EndState) {
            image = gaService.createImage(containerShape, "../graph/end.png");
        } else if (node instanceof Decision) {
            image = gaService.createImage(containerShape, "../graph/decision.png");
        } else if (node instanceof Fork) {
            image = gaService.createImage(containerShape, "../graph/fork_join.png");
        } else if (node instanceof Join) {
            image = gaService.createImage(containerShape, "../graph/fork_join.png");
        } else if (node instanceof SendMessageNode) {
            image = gaService.createImage(containerShape, "../graph/sendmessage.png");
        } else if (node instanceof ReceiveMessageNode) {
            image = gaService.createImage(containerShape, "../graph/receivemessage.png");
        } else {
            throw new IllegalArgumentException("Unsupported node: " + node);
        }
        gaService.setLocationAndSize(image, context.getX(), context.getY(), GRID_SIZE * 4, GRID_SIZE * 4);
        link(containerShape, node);
        createService.createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        node.setConstraint(new org.eclipse.draw2d.geometry.Rectangle(context.getX(), context.getY(), GRID_SIZE * 4, GRID_SIZE * 4));
        return containerShape;
    }
}

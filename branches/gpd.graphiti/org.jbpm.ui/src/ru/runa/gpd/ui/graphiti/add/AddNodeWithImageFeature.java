package ru.runa.gpd.ui.graphiti.add;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.jbpm.ui.common.model.Decision;
import org.jbpm.ui.common.model.Fork;
import org.jbpm.ui.common.model.Join;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.Subprocess;
import org.jbpm.ui.jpdl3.model.ReceiveMessageNode;
import org.jbpm.ui.jpdl3.model.SendMessageNode;

public class AddNodeWithImageFeature extends AbstractAddShapeFeature {
	
    public AddNodeWithImageFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public PictogramElement add(IAddContext context) {
    	final int GRID_SIZE = 12;
    	Node node = (Node) context.getNewObject();
    	
        ContainerShape parent = context.getTargetContainer();
        IPeCreateService createService = Graphiti.getPeCreateService();
        ContainerShape containerShape = createService.createContainerShape(parent, true);
        IGaService gaService = Graphiti.getGaService();
        
        Image image = null;
        if (Decision.class.isInstance(node))
        	image = gaService.createImage(containerShape, "../graph/decision.png");
        else if (Fork.class.isInstance(node))
        	image = gaService.createImage(containerShape, "../graph/fork_join.png");
        else if (Join.class.isInstance(node))
        	image = gaService.createImage(containerShape, "../graph/fork_join.png");
        else if (SendMessageNode.class.isInstance(node))
        	image = gaService.createImage(containerShape, "../graph/sendmessage.png");
        else if (ReceiveMessageNode.class.isInstance(node))
        	image = gaService.createImage(containerShape, "../graph/receivemessage.png");
        else image = gaService.createImage(containerShape, "");
        
        gaService.setLocationAndSize(image, context.getX()-GRID_SIZE*2, context.getY()-GRID_SIZE*2, GRID_SIZE*4, GRID_SIZE*4);
        link(containerShape, node);
        createService.createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        node.setConstraint(new org.eclipse.draw2d.geometry.Rectangle(context.getX(), context.getY(), context.getWidth(), context.getHeight()));
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

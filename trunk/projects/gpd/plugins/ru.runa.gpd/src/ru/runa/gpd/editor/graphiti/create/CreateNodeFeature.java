package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.add.AddNodeFeature;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class CreateNodeFeature extends AbstractCreateFeature implements GEFConstants {
    public static final String CONNECTION_PROPERTY = "connectionContext";
    private NodeTypeDefinition nodeDefinition;
    private DiagramFeatureProvider featureProvider;

    public CreateNodeFeature() {
        super(null, "", "");
    }

    public void setNodeDefinition(NodeTypeDefinition nodeDefinition) {
        this.nodeDefinition = nodeDefinition;
    }

    public void setFeatureProvider(DiagramFeatureProvider provider) {
        this.featureProvider = provider;
    }

    @Override
    public DiagramFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public String getCreateName() {
        return nodeDefinition.getLabel();
    }

    @Override
    public String getCreateImageId() {
        return getNodeDefinition().getPaletteIcon();
    }

    public NodeTypeDefinition getNodeDefinition() {
        return nodeDefinition;
    }

    protected ProcessDefinition getProcessDefinition() {
        return getFeatureProvider().getCurrentProcessDefinition();
    }

    @Override
    public boolean canCreate(ICreateContext context) {
        //        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        //        return (context.getTargetContainer() instanceof Diagram || parentObject instanceof Subprocess);
        return true;
    }

    @Override
    public Object[] create(ICreateContext context) {
        Node node = getNodeDefinition().createElement(getProcessDefinition());
        ContainerShape targetContainer = context.getTargetContainer();
        if (targetContainer instanceof Diagram) {
            getProcessDefinition().addChild(node);
        } else {
            Object parent = getBusinessObjectForPictogramElement(targetContainer);
            ((Node) parent).addChild(node);
        }
        CreateConnectionContext connectionContext = (CreateConnectionContext) context.getProperty(CONNECTION_PROPERTY);
        setLocation(node, (CreateContext) context, connectionContext);
        PictogramElement element = addGraphicalRepresentation(context, node);
        if (connectionContext != null) {
            connectionContext.setTargetPictogramElement(element);
            connectionContext.setTargetAnchor(Graphiti.getPeService().getChopboxAnchor((AnchorContainer) element));
            CreateTransitionFeature createTransitionFeature = new CreateTransitionFeature();
            createTransitionFeature.setFeatureProvider(featureProvider);
            createTransitionFeature.create(connectionContext);
        }
        return new Object[] { node };
    }

    private void setLocation(Node target, CreateContext context, CreateConnectionContext connectionContext) {
        if (connectionContext != null) {
            PictogramElement sourceElement = connectionContext.getSourcePictogramElement();
            int xRight = sourceElement.getGraphicsAlgorithm().getX() + sourceElement.getGraphicsAlgorithm().getWidth();
            //int yCenter = sourceElement.getGraphicsAlgorithm().getY() + sourceElement.getGraphicsAlgorithm().getHeight() / 2;
            Dimension targetSize = ((AddNodeFeature) getFeatureProvider().getAddFeature(target.getClass())).getDefaultSize();
            int yDelta = (targetSize.height - sourceElement.getGraphicsAlgorithm().getHeight()) / 2;
            context.setLocation(xRight + 100, sourceElement.getGraphicsAlgorithm().getY() - yDelta);
        }
    }
}

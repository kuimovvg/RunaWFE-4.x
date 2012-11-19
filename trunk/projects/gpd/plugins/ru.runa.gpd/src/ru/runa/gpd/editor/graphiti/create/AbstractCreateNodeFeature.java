package ru.runa.gpd.editor.graphiti.create;

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
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class AbstractCreateNodeFeature extends AbstractCreateFeature implements GEFConstants {
    public static final String CONNECTION_PROPERTY = "connectionContext";
    private final NodeTypeDefinition nodeDefinition;

    public AbstractCreateNodeFeature(DiagramFeatureProvider provider, Class<? extends GraphElement> nodeClass) {
        super(provider, "", "");
        this.nodeDefinition = NodeRegistry.getNodeTypeDefinition(nodeClass);
    }

    @Override
    public String getCreateName() {
        return nodeDefinition.getLabel();
    }

    @Override
    public String getCreateImageId() {
        return getNodeDefinition().getGEFPaletteEntry().getImageName();
    }

    public NodeTypeDefinition getNodeDefinition() {
        return nodeDefinition;
    }

    protected ProcessDefinition getProcessDefinition() {
        return ((DiagramFeatureProvider) getFeatureProvider()).getCurrentProcessDefinition();
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
            CreateTransitionFeature createTransitionFeature = new CreateTransitionFeature(getFeatureProvider());
            createTransitionFeature.create(connectionContext);
        }
        return new Object[] { node };
    }

    private void setLocation(Node target, CreateContext context, CreateConnectionContext connectionContext) {
        if (connectionContext != null) {
            PictogramElement sourceElement = connectionContext.getSourcePictogramElement();
            Object source = getBusinessObjectForPictogramElement(sourceElement);
            int h = sourceElement.getGraphicsAlgorithm().getHeight();
            //getFeatureProvider().getAddFeature(context);
            //            if (source instanceof Event && (target instanceof Task || target instanceof CallActivity)) {
            context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 200, sourceElement.getGraphicsAlgorithm().getY() - 2 * GRID_SIZE);
            //            } else if (source instanceof Event && target instanceof Gateway) {
            //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 80, sourceElement.getGraphicsAlgorithm().getY() - 3);
            //            } else if (source instanceof Gateway && target instanceof Event) {
            //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 85, sourceElement.getGraphicsAlgorithm().getY() + 3);
            //            } else if (source instanceof Gateway && (target instanceof Task || target instanceof CallActivity)) {
            //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 85, sourceElement.getGraphicsAlgorithm().getY() - 7);
            //            } else if ((source instanceof Task || source instanceof CallActivity) && target instanceof Gateway) {
            //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 160, sourceElement.getGraphicsAlgorithm().getY() + 7);
            //            } else if ((source instanceof Task || source instanceof CallActivity) && target instanceof Event) {
            //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 160, sourceElement.getGraphicsAlgorithm().getY() + 10);
            //            } else if ((source instanceof Task || source instanceof CallActivity) && (target instanceof Task || target instanceof CallActivity)) {
            //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 160, sourceElement.getGraphicsAlgorithm().getY());
            //            }
        }
    }
}

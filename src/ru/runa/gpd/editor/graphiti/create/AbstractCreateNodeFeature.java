package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;

public abstract class AbstractCreateNodeFeature extends AbstractCreateFeature {
    private static final String CONNECTION_ATTRIBUTE = "org.activiti.designer.connectionContext";
    private final NodeTypeDefinition nodeDefinition;

    public AbstractCreateNodeFeature(DiagramFeatureProvider provider) {
        super(provider, "", "");
        this.nodeDefinition = NodeRegistry.getNodeTypeDefinition(getNodeId());
    }

    protected abstract String getNodeId();

    @Override
    public String getCreateName() {
        return nodeDefinition.getLabel();
    }

    protected NodeTypeDefinition getNodeDefinition() {
        return nodeDefinition;
    }

    protected ProcessDefinition getProcessDefinition() {
        return ((DiagramFeatureProvider) getFeatureProvider()).getCurrentProcessDefinition();
    }

    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        return (context.getTargetContainer() instanceof Diagram || parentObject instanceof Subprocess);
    }

    @Override
    public Object[] create(ICreateContext context) {
        Node element = getNodeDefinition().createElement(getProcessDefinition());
        addObjectToContainer(context, element);
        return new Object[] { element };
    }

    @Override
    public String getCreateImageId() {
        return getNodeDefinition().getGEFPaletteEntry().getImageName();
    }

    /**
     * Adds the given base element to the context. At first, a new ID is generated for the new object.
     * Depending on the type of element, it is added as artifact or flow element.
     * 
     * @param context the context to add it
     * @param node the base element to add
     */
    protected void addObjectToContainer(ICreateContext context, Node node) {
        ContainerShape targetContainer = context.getTargetContainer();
        if (targetContainer instanceof Diagram) {
            getProcessDefinition().addChild(node);
        } else {
            Object parent = getBusinessObjectForPictogramElement(targetContainer);
            ((Node) parent).addChild(node);
        }
        addGraphicalContent(context, node);
    }

    protected void addGraphicalContent(ICreateContext context, Node targetElement) {
        setLocation(targetElement, (CreateContext) context);
        PictogramElement element = addGraphicalRepresentation(context, targetElement);
        createConnectionIfNeeded(element, context);
        Anchor elementAnchor = null;
        EList<Anchor> anchorList = ((ContainerShape) element).getAnchors();
        for (Anchor anchor : anchorList) {
            if (anchor instanceof ChopboxAnchor) {
                elementAnchor = anchor;
                break;
            }
        }
        //        if (context.getProperty("org.activiti.designer.changetype.sourceflows") != null) {
        //            List<SequenceFlow> sourceFlows = (List<SequenceFlow>) context.getProperty("org.activiti.designer.changetype.sourceflows");
        //            for (SequenceFlow sourceFlow : sourceFlows) {
        //                sourceFlow.setSourceRef((Node) targetElement);
        //                Connection connection = (Connection) getFeatureProvider().getPictogramElementForBusinessObject(sourceFlow);
        //                connection.setStart(elementAnchor);
        //                elementAnchor.getOutgoingConnections().add(connection);
        //            }
        //            List<SequenceFlow> targetFlows = (List<SequenceFlow>) context.getProperty("org.activiti.designer.changetype.targetflows");
        //            for (SequenceFlow targetFlow : targetFlows) {
        //                targetFlow.setTargetRef((Node) targetElement);
        //                Connection connection = (Connection) getFeatureProvider().getPictogramElementForBusinessObject(targetFlow);
        //                connection.setEnd(elementAnchor);
        //                elementAnchor.getIncomingConnections().add(connection);
        //            }
        //        }
    }

    protected void setName(String defaultName, Node targetElement, ICreateContext context) {
        if (context.getProperty("org.activiti.designer.changetype.name") != null) {
            targetElement.setName(context.getProperty("org.activiti.designer.changetype.name").toString());
        } else {
            targetElement.setName(defaultName);
        }
    }

    private void setLocation(Node targetElement, CreateContext context) {
        //        if (context.getProperty(CONNECTION_ATTRIBUTE) != null) {
        //            CreateConnectionContext connectionContext = (CreateConnectionContext) context.getProperty(CONNECTION_ATTRIBUTE);
        //            PictogramElement sourceElement = connectionContext.getSourcePictogramElement();
        //            Object sourceObject = getBusinessObjectForPictogramElement(sourceElement);
        //            if (sourceObject instanceof Event && (targetElement instanceof Task || targetElement instanceof CallActivity)) {
        //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 80, sourceElement.getGraphicsAlgorithm().getY() - 10);
        //            } else if (sourceObject instanceof Event && targetElement instanceof Gateway) {
        //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 80, sourceElement.getGraphicsAlgorithm().getY() - 3);
        //            } else if (sourceObject instanceof Gateway && targetElement instanceof Event) {
        //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 85, sourceElement.getGraphicsAlgorithm().getY() + 3);
        //            } else if (sourceObject instanceof Gateway && (targetElement instanceof Task || targetElement instanceof CallActivity)) {
        //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 85, sourceElement.getGraphicsAlgorithm().getY() - 7);
        //            } else if ((sourceObject instanceof Task || sourceObject instanceof CallActivity) && targetElement instanceof Gateway) {
        //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 160, sourceElement.getGraphicsAlgorithm().getY() + 7);
        //            } else if ((sourceObject instanceof Task || sourceObject instanceof CallActivity) && targetElement instanceof Event) {
        //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 160, sourceElement.getGraphicsAlgorithm().getY() + 10);
        //            } else if ((sourceObject instanceof Task || sourceObject instanceof CallActivity)
        //                    && (targetElement instanceof Task || targetElement instanceof CallActivity)) {
        //                context.setLocation(sourceElement.getGraphicsAlgorithm().getX() + 160, sourceElement.getGraphicsAlgorithm().getY());
        //            }
        //        }
    }

    private void createConnectionIfNeeded(PictogramElement element, ICreateContext context) {
        //        if (context.getProperty(CONNECTION_ATTRIBUTE) != null) {
        //            CreateConnectionContext connectionContext = (CreateConnectionContext) context.getProperty(CONNECTION_ATTRIBUTE);
        //            connectionContext.setTargetPictogramElement(element);
        //            connectionContext.setTargetAnchor(Graphiti.getPeService().getChopboxAnchor((AnchorContainer) element));
        //            CreateSequenceFlowFeature sequenceFeature = new CreateSequenceFlowFeature(getFeatureProvider());
        //            sequenceFeature.create(connectionContext);
        //        }
    }
}

package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;

import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class CreateTransitionFeature extends AbstractCreateConnectionFeature {
    private final NodeTypeDefinition transitionDefinition;

    public CreateTransitionFeature(IFeatureProvider provider) {
        super(provider, "", "");
        this.transitionDefinition = NodeRegistry.getNodeTypeDefinition("transition");
    }

    @Override
    public String getCreateName() {
        return transitionDefinition.getLabel();
    }

    @Override
    public boolean canStartConnection(ICreateConnectionContext context) {
        Node source = getFlowNode(context.getSourceAnchor());
        // return true if source anchor isn't undefined
        if (source != null && !(source instanceof EndState)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canCreate(ICreateConnectionContext context) {
        Node source = getFlowNode(context.getSourceAnchor());
        Node target = getFlowNode(context.getTargetAnchor());
        if (source != null && target != null && source != target) {
            return true;
        }
        return false;
    }

    @Override
    public Connection create(ICreateConnectionContext context) {
        Node source = getFlowNode(context.getSourceAnchor());
        Node target = getFlowNode(context.getTargetAnchor());
        if (source != null && target != null) {
            // create new business object
            Transition transition = new Transition();
            transition.setParent(source);
            transition.setTarget(target);
            transition.setName(source.getNextTransitionName());
            // add connection for business object
            AddConnectionContext addConnectionContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
            addConnectionContext.setNewObject(transition);
            return (Connection) getFeatureProvider().addIfPossible(addConnectionContext);
        }
        return null;
    }

    private Node getFlowNode(Anchor anchor) {
        if (anchor != null) {
            Object obj = getBusinessObjectForPictogramElement(anchor.getParent());
            if (obj instanceof Node) {
                return (Node) obj;
            }
        }
        return null;
    }

    @Override
    public String getCreateImageId() {
        return transitionDefinition.getGEFPaletteEntry().getImageName();
    }
}

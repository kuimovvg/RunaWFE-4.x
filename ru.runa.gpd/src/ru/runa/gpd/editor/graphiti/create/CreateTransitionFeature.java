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
    private IFeatureProvider featureProvider;

    public CreateTransitionFeature() {
        super(null, "", "");
        this.transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
    }

    public void setFeatureProvider(IFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public String getCreateName() {
        return transitionDefinition.getLabel();
    }

    @Override
    public String getCreateImageId() {
        return transitionDefinition.getPaletteIcon();
    }

    @Override
    public boolean canStartConnection(ICreateConnectionContext context) {
        Node source = getNode(context.getSourceAnchor());
        if (source != null && !(source instanceof EndState)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canCreate(ICreateConnectionContext context) {
        Node source = getNode(context.getSourceAnchor());
        Node target = getNode(context.getTargetAnchor());
        return (source != null && target != null && source != target);
    }

    @Override
    public Connection create(ICreateConnectionContext context) {
        Node source = getNode(context.getSourceAnchor());
        Node target = getNode(context.getTargetAnchor());
        // create new business object
        Transition transition = transitionDefinition.createElement(source);
        transition.setTarget(target);
        transition.setName(source.getNextTransitionName());
        source.addLeavingTransition(transition);
        // add connection for business object
        AddConnectionContext addConnectionContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
        addConnectionContext.setNewObject(transition);
        return (Connection) getFeatureProvider().addIfPossible(addConnectionContext);
    }

    private Node getNode(Anchor anchor) {
        if (anchor != null) {
            return (Node) getBusinessObjectForPictogramElement(anchor.getParent());
        }
        return null;
    }
}

package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.tb.ContextButtonEntry;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IContextButtonPadData;

import ru.runa.gpd.editor.graphiti.create.CreateNodeFeature;
import ru.runa.gpd.editor.graphiti.update.OpenSubProcessFeature;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;

public class DiagramToolBehaviorProvider extends DefaultToolBehaviorProvider {
    public DiagramToolBehaviorProvider(IDiagramTypeProvider provider) {
        super(provider);
    }

    @Override
    protected DiagramFeatureProvider getFeatureProvider() {
        return (DiagramFeatureProvider) super.getFeatureProvider();
    }

    @Override
    public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
        PictogramElement pe = context.getInnerPictogramElement();
        GraphElement element = (GraphElement) getFeatureProvider().getBusinessObjectForPictogramElement(pe);
        if (element instanceof Subprocess) {
            return new OpenSubProcessFeature(getFeatureProvider());
        }
        return super.getDoubleClickFeature(context);
    }

    @Override
    public IContextButtonPadData getContextButtonPad(IPictogramElementContext context) {
        IContextButtonPadData data = super.getContextButtonPad(context);
        PictogramElement pe = context.getPictogramElement();
        setGenericContextButtons(data, pe, CONTEXT_BUTTON_DELETE);
        GraphElement element = (GraphElement) getFeatureProvider().getBusinessObjectForPictogramElement(pe);
        //
        CreateConnectionContext createConnectionContext = new CreateConnectionContext();
        createConnectionContext.setSourcePictogramElement(pe);
        Anchor connectionAnchor = null;
        if (pe instanceof Anchor) {
            connectionAnchor = (Anchor) pe;
        } else if (pe instanceof AnchorContainer) {
            connectionAnchor = Graphiti.getPeService().getChopboxAnchor((AnchorContainer) pe);
        }
        createConnectionContext.setSourceAnchor(connectionAnchor);
        if (!(pe.eContainer() instanceof ContainerShape)) {
            return data;
        }
        //
        CreateContext createContext = new CreateContext();
        createContext.setTargetContainer((ContainerShape) pe.eContainer());
        createContext.putProperty(CreateNodeFeature.CONNECTION_PROPERTY, createConnectionContext);
        //
        NodeTypeDefinition taskStateDefinition = NodeRegistry.getNodeTypeDefinition(TaskState.class);
        CreateNodeFeature createTaskStateFeature = new CreateNodeFeature();
        createTaskStateFeature.setNodeDefinition(taskStateDefinition);
        createTaskStateFeature.setFeatureProvider(getFeatureProvider());
        ContextButtonEntry createTaskStateButton = new ContextButtonEntry(createTaskStateFeature, createContext);
        createTaskStateButton.setText(taskStateDefinition.getLabel());
        createTaskStateButton.setIconId(taskStateDefinition.getPaletteIcon());
        data.getDomainSpecificContextButtons().add(createTaskStateButton);
        //
        NodeTypeDefinition decisionDefinition = NodeRegistry.getNodeTypeDefinition(Decision.class);
        CreateNodeFeature decisionFeature = new CreateNodeFeature();
        decisionFeature.setNodeDefinition(decisionDefinition);
        decisionFeature.setFeatureProvider(getFeatureProvider());
        ContextButtonEntry createDecisionButton = new ContextButtonEntry(decisionFeature, createContext);
        createDecisionButton.setText(decisionDefinition.getLabel());
        createDecisionButton.setIconId(decisionDefinition.getPaletteIcon());
        //createDecisionButton.addDragAndDropFeature(decisionFeature);
        data.getDomainSpecificContextButtons().add(createDecisionButton);
        //
        NodeTypeDefinition endStateDefinition = NodeRegistry.getNodeTypeDefinition(EndState.class);
        CreateNodeFeature endFeature = new CreateNodeFeature();
        endFeature.setNodeDefinition(endStateDefinition);
        endFeature.setFeatureProvider(getFeatureProvider());
        ContextButtonEntry createEndStateButton = new ContextButtonEntry(endFeature, createContext);
        createEndStateButton.setText(endStateDefinition.getLabel());
        createEndStateButton.setIconId(endStateDefinition.getPaletteIcon());
        data.getDomainSpecificContextButtons().add(createEndStateButton);
        //
        ContextButtonEntry createTransitionButton = new ContextButtonEntry(null, context);
        NodeTypeDefinition transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
        createTransitionButton.setText(transitionDefinition.getLabel());
        createTransitionButton.setIconId(transitionDefinition.getPaletteIcon());
        ICreateConnectionFeature[] features = getFeatureProvider().getCreateConnectionFeatures();
        for (ICreateConnectionFeature feature : features) {
            if (feature.isAvailable(createConnectionContext) && feature.canStartConnection(createConnectionContext)) {
                createTransitionButton.addDragAndDropFeature(feature);
            }
        }
        if (createTransitionButton.getDragAndDropFeatures().size() > 0) {
            data.getDomainSpecificContextButtons().add(createTransitionButton);
        }
        //        
        if (!(element instanceof EndState)) {
            ContextButtonEntry createElementButton = new ContextButtonEntry(null, null);
            createElementButton.setText("new element");
            createElementButton.setDescription("Create a new element");
            createElementButton.setIconId("?.png");
            data.getDomainSpecificContextButtons().add(createElementButton);
            for (ICreateFeature feature : getFeatureProvider().getCreateFeatures()) {
                if (feature instanceof CreateNodeFeature && feature.canCreate(createContext)) {
                    CreateNodeFeature createNodeFeature = (CreateNodeFeature) feature;
                    ContextButtonEntry createButton = new ContextButtonEntry(feature, createContext);
                    NodeTypeDefinition typeDefinition = createNodeFeature.getNodeDefinition();
                    createButton.setText(typeDefinition.getLabel());
                    createButton.setIconId(typeDefinition.getPaletteIcon());
                    createElementButton.getContextButtonMenuEntries().add(createButton);
                }
            }
        }
        return data;
    }
}

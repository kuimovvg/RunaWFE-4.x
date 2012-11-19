package ru.runa.gpd.editor.graphiti;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.context.ICustomContext;
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
import org.eclipse.graphiti.tb.ContextMenuEntry;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IContextButtonPadData;
import org.eclipse.graphiti.tb.IContextMenuEntry;

import ru.runa.gpd.editor.graphiti.create.AbstractCreateNodeFeature;
import ru.runa.gpd.editor.graphiti.create.CreateEndStateFeature;
import ru.runa.gpd.editor.graphiti.create.CreateTaskStateFeature;
import ru.runa.gpd.editor.graphiti.update.OpenSubProcessFeature;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;

public class DiagramToolBehaviorProvider extends DefaultToolBehaviorProvider {
    public DiagramToolBehaviorProvider(IDiagramTypeProvider provider) {
        super(provider);
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
        setGenericContextButtons(data, pe, CONTEXT_BUTTON_DELETE | CONTEXT_BUTTON_UPDATE);
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
        createContext.putProperty(AbstractCreateNodeFeature.CONNECTION_PROPERTY, createConnectionContext);
        if (element instanceof StartState) {
            CreateTaskStateFeature createTaskStateFeature = new CreateTaskStateFeature((DiagramFeatureProvider) getFeatureProvider());
            ContextButtonEntry createTaskStateButton = new ContextButtonEntry(createTaskStateFeature, createContext);
            NodeTypeDefinition taskStateDefinition = NodeRegistry.getNodeTypeDefinition(TaskState.class);
            createTaskStateButton.setText(taskStateDefinition.getLabel());
            createTaskStateButton.setIconId(taskStateDefinition.getGEFPaletteEntry().getImageName());
            data.getDomainSpecificContextButtons().add(createTaskStateButton);
            //
            CreateEndStateFeature endFeature = new CreateEndStateFeature((DiagramFeatureProvider) getFeatureProvider());
            ContextButtonEntry createEndStateButton = new ContextButtonEntry(endFeature, createContext);
            NodeTypeDefinition endStateDefinition = NodeRegistry.getNodeTypeDefinition(EndState.class);
            createEndStateButton.setText(endStateDefinition.getLabel());
            createEndStateButton.setIconId(endStateDefinition.getGEFPaletteEntry().getImageName());
            data.getDomainSpecificContextButtons().add(createEndStateButton);
        }
        //
        ContextButtonEntry createTransitionButton = new ContextButtonEntry(null, context);
        NodeTypeDefinition transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
        createTransitionButton.setText(transitionDefinition.getLabel());
        createTransitionButton.setIconId(transitionDefinition.getGEFPaletteEntry().getImageName());
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
                if (feature instanceof AbstractCreateNodeFeature && feature.canCreate(createContext)) {
                    AbstractCreateNodeFeature createNodeFeature = (AbstractCreateNodeFeature) feature;
                    ContextButtonEntry createButton = new ContextButtonEntry(feature, createContext);
                    NodeTypeDefinition typeDefinition = createNodeFeature.getNodeDefinition();
                    createButton.setText(typeDefinition.getLabel());
                    createButton.setIconId(typeDefinition.getGEFPaletteEntry().getImageName());
                    createElementButton.getContextButtonMenuEntries().add(createButton);
                }
            }
        }
        return data;
    }

    @Override
    public IContextMenuEntry[] getContextMenu(ICustomContext context) {
        List<IContextMenuEntry> menuList = new ArrayList<IContextMenuEntry>();
        if (context.getPictogramElements() != null) {
            for (PictogramElement pictogramElement : context.getPictogramElements()) {
                if (getFeatureProvider().getBusinessObjectForPictogramElement(pictogramElement) == null) {
                    continue;
                }
                //Object object = getFeatureProvider().getBusinessObjectForPictogramElement(pictogramElement);
                ContextMenuEntry subMenuDelete = new ContextMenuEntry(new SaveBpmnModelFeature(getFeatureProvider()), context);
                subMenuDelete.setText("SAVE BPMN");
                subMenuDelete.setSubmenu(false);
                menuList.add(subMenuDelete);
                //                } else if (object instanceof Association) {
                //                    final ContextMenuEntry subMenuDelete = new ContextMenuEntry(new DeleteAssociationFeature(getFeatureProvider()), context);
                //                    subMenuDelete.setText(subMenuDelete.getFeature().getDescription());
                //                    subMenuDelete.setSubmenu(false);
                //                    menuList.add(subMenuDelete);
                //                } else if (object instanceof Pool) {
                //                    ContextMenuEntry subMenuDelete = new ContextMenuEntry(new DeletePoolFeature(getFeatureProvider()), context);
                //                    subMenuDelete.setText("Delete pool"); //$NON-NLS-1$
                //                    subMenuDelete.setSubmenu(false);
                //                    menuList.add(subMenuDelete);
                //                }
            }
        }
        return menuList.toArray(new IContextMenuEntry[menuList.size()]);
    }
}

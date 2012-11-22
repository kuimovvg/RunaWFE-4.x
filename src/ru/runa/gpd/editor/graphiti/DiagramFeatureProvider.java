package ru.runa.gpd.editor.graphiti;

import java.util.List;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddBendpointFeature;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.IMoveBendpointFeature;
import org.eclipse.graphiti.features.IMoveShapeFeature;
import org.eclipse.graphiti.features.IReconnectionFeature;
import org.eclipse.graphiti.features.IRemoveBendpointFeature;
import org.eclipse.graphiti.features.IResizeShapeFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddBendpointContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.IMoveBendpointContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.IRemoveBendpointContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;

import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.editor.graphiti.add.AddTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.update.DeleteElementFeature;
import ru.runa.gpd.editor.graphiti.update.DirectEditNodeNameFeature;
import ru.runa.gpd.editor.graphiti.update.MoveNodeFeature;
import ru.runa.gpd.editor.graphiti.update.MoveTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.update.ReconnectSequenceFlowFeature;
import ru.runa.gpd.editor.graphiti.update.RemoveTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.update.ResizeNodeFeature;
import ru.runa.gpd.editor.graphiti.update.UpdateNodeFeature;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.ProjectFinder;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class DiagramFeatureProvider extends DefaultFeatureProvider {
    public DiagramFeatureProvider(IDiagramTypeProvider dtp) {
        super(dtp);
        setIndependenceSolver(new IndependenceSolver());
    }

    public ProcessDefinition getCurrentProcessDefinition() {
        return ProcessCache.getProcessDefinition(ProjectFinder.getCurrentFile());
    }

    @Override
    public ICreateFeature[] getCreateFeatures() {
        List<ICreateFeature> list = Lists.newArrayList();
        for (NodeTypeDefinition definition : NodeRegistry.getDefinitions()) {
            if (definition.getGraphitiEntry() != null && NodeTypeDefinition.TYPE_NODE.equals(definition.getType())) {
                if (!Strings.isNullOrEmpty(definition.getBpmnElementName())) {
                    list.add((ICreateFeature) definition.getGraphitiEntry().createCreateFeature(this));
                }
            }
        }
        return list.toArray(new ICreateFeature[list.size()]);
    }

    public IAddFeature getAddFeature(Class<? extends GraphElement> nodeClass) {
        NodeTypeDefinition definition = NodeRegistry.getNodeTypeDefinition(nodeClass);
        if (definition != null && definition.getGraphitiEntry() != null) {
            return definition.getGraphitiEntry().createAddFeature(this);
        }
        return null;
    }

    @Override
    public IAddFeature getAddFeature(IAddContext context) {
        return getAddFeature((Class<? extends GraphElement>) context.getNewObject().getClass());
    }

    @Override
    public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
        return new MoveNodeFeature(this);
    }

    @Override
    public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
        return new ResizeNodeFeature(this);
    }

    @Override
    public IDeleteFeature getDeleteFeature(IDeleteContext context) {
        return new DeleteElementFeature(this);
    }

    @Override
    public ICreateConnectionFeature[] getCreateConnectionFeatures() {
        List<ICreateConnectionFeature> list = Lists.newArrayList();
        for (NodeTypeDefinition definition : NodeRegistry.getDefinitions()) {
            if (definition.getGraphitiEntry() != null && NodeTypeDefinition.TYPE_CONNECTION.equals(definition.getType())) {
                list.add((ICreateConnectionFeature) definition.getGraphitiEntry().createCreateFeature(this));
            }
        }
        return list.toArray(new ICreateConnectionFeature[list.size()]);
    }

    @Override
    public IAddBendpointFeature getAddBendpointFeature(IAddBendpointContext context) {
        return new AddTransitionBendpointFeature(this);
    }

    @Override
    public IMoveBendpointFeature getMoveBendpointFeature(IMoveBendpointContext context) {
        return new MoveTransitionBendpointFeature(this);
    }

    @Override
    public IRemoveBendpointFeature getRemoveBendpointFeature(IRemoveBendpointContext context) {
        return new RemoveTransitionBendpointFeature(this);
    }

    @Override
    public IReconnectionFeature getReconnectionFeature(IReconnectionContext context) {
        return new ReconnectSequenceFlowFeature(this);
    }

    @Override
    public IDirectEditingFeature getDirectEditingFeature(IDirectEditingContext context) {
        PictogramElement pe = context.getPictogramElement();
        Object bo = getBusinessObjectForPictogramElement(pe);
        if (bo instanceof Node) {
            return new DirectEditNodeNameFeature(this);
        }
        return null;
    }

    @Override
    public IUpdateFeature getUpdateFeature(IUpdateContext context) {
        PictogramElement pictogramElement = context.getPictogramElement();
        Object bo = getBusinessObjectForPictogramElement(pictogramElement);
        if (bo instanceof Node) {
            return new UpdateNodeFeature(this);
        }
        return super.getUpdateFeature(context);
    }
    //    @Override
    //    public IFeature[] getDragAndDropFeatures(IPictogramElementContext context) {
    //        // simply return all create connection features
    //        return getCreateConnectionFeatures();
    //    }
    //    @Override
    //    public ICopyFeature getCopyFeature(ICopyContext context) {
    //        return new CopyFlowElementFeature(this);
    //    }
    //    @Override
    //    public IPasteFeature getPasteFeature(IPasteContext context) {
    //        return new PasteFlowElementFeature(this);
    //    }
    //    
}

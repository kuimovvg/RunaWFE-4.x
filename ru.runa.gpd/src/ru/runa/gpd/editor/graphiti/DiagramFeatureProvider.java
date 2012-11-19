package ru.runa.gpd.editor.graphiti;

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
import ru.runa.gpd.editor.graphiti.add.AddMultiInstanceFeature;
import ru.runa.gpd.editor.graphiti.add.AddNodeFeature;
import ru.runa.gpd.editor.graphiti.add.AddNodeWithImageFeature;
import ru.runa.gpd.editor.graphiti.add.AddStateFeature;
import ru.runa.gpd.editor.graphiti.add.AddSubProcessFeature;
import ru.runa.gpd.editor.graphiti.add.AddTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.add.AddTransitionFeature;
import ru.runa.gpd.editor.graphiti.create.CreateDecisionFeature;
import ru.runa.gpd.editor.graphiti.create.CreateEndStateFeature;
import ru.runa.gpd.editor.graphiti.create.CreateForkFeature;
import ru.runa.gpd.editor.graphiti.create.CreateJoinFeature;
import ru.runa.gpd.editor.graphiti.create.CreateMultiInstanceFeature;
import ru.runa.gpd.editor.graphiti.create.CreateReceiveMessageFeature;
import ru.runa.gpd.editor.graphiti.create.CreateSendMessageFeature;
import ru.runa.gpd.editor.graphiti.create.CreateStartNodeFeature;
import ru.runa.gpd.editor.graphiti.create.CreateSubProcessFeature;
import ru.runa.gpd.editor.graphiti.create.CreateTaskStateFeature;
import ru.runa.gpd.editor.graphiti.create.CreateTransitionFeature;
import ru.runa.gpd.editor.graphiti.update.DeleteElementFeature;
import ru.runa.gpd.editor.graphiti.update.DirectEditNodeNameFeature;
import ru.runa.gpd.editor.graphiti.update.MoveNodeFeature;
import ru.runa.gpd.editor.graphiti.update.MoveTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.update.ReconnectSequenceFlowFeature;
import ru.runa.gpd.editor.graphiti.update.RemoveTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.update.ResizeNodeFeature;
import ru.runa.gpd.editor.graphiti.update.UpdateNodeFeature;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Fork;
import ru.runa.gpd.lang.model.Join;
import ru.runa.gpd.lang.model.MultiInstance;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ReceiveMessageNode;
import ru.runa.gpd.lang.model.SendMessageNode;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.util.ProjectFinder;

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
        return new ICreateFeature[] { new CreateStartNodeFeature(this), new CreateTaskStateFeature(this), new CreateEndStateFeature(this), new CreateDecisionFeature(this),
                new CreateForkFeature(this), new CreateJoinFeature(this), new CreateSendMessageFeature(this), new CreateReceiveMessageFeature(this),
                new CreateSubProcessFeature(this), new CreateMultiInstanceFeature(this) };
    }

    @Override
    public IAddFeature getAddFeature(IAddContext context) {
        if (context.getNewObject() instanceof TaskState) {
            return new AddStateFeature(this);
        }
        if (context.getNewObject() instanceof Subprocess) {
            return new AddSubProcessFeature(this);
        }
        if (context.getNewObject() instanceof MultiInstance) {
            return new AddMultiInstanceFeature(this);
        }
        if (context.getNewObject() instanceof StartState || context.getNewObject() instanceof EndState || context.getNewObject() instanceof Decision
                || context.getNewObject() instanceof Fork || context.getNewObject() instanceof Join || context.getNewObject() instanceof SendMessageNode
                || context.getNewObject() instanceof ReceiveMessageNode) {
            return new AddNodeWithImageFeature(this);
        }
        if (context.getNewObject() instanceof Node) {
            return new AddNodeFeature(this);
        }
        if (context.getNewObject() instanceof Transition) {
            return new AddTransitionFeature(this);
        }
        return null;
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
        return new ICreateConnectionFeature[] { new CreateTransitionFeature(this) };
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

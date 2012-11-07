package ru.runa.gpd.ui.graphiti;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.IMoveShapeFeature;
import org.eclipse.graphiti.features.IResizeShapeFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
import org.jbpm.ui.ProcessCache;
import org.jbpm.ui.common.model.Decision;
import org.jbpm.ui.common.model.Fork;
import org.jbpm.ui.common.model.Join;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Subprocess;
import org.jbpm.ui.common.model.Transition;
import org.jbpm.ui.jpdl3.model.MultiInstance;
import org.jbpm.ui.jpdl3.model.ReceiveMessageNode;
import org.jbpm.ui.jpdl3.model.SendMessageNode;
import org.jbpm.ui.jpdl3.model.TaskState;
import org.jbpm.ui.util.ProjectFinder;

import ru.runa.gpd.ui.graphiti.add.AddMultiInstanceFeature;
import ru.runa.gpd.ui.graphiti.add.AddNodeWithImageFeature;
import ru.runa.gpd.ui.graphiti.add.AddStateFeature;
import ru.runa.gpd.ui.graphiti.add.AddNodeFeature;
import ru.runa.gpd.ui.graphiti.add.AddSubProcessFeature;
import ru.runa.gpd.ui.graphiti.add.AddTransitionFeature;
import ru.runa.gpd.ui.graphiti.create.CreateDecisionFeature;
import ru.runa.gpd.ui.graphiti.create.CreateEndEventFeature;
import ru.runa.gpd.ui.graphiti.create.CreateForkFeature;
import ru.runa.gpd.ui.graphiti.create.CreateJoinFeature;
import ru.runa.gpd.ui.graphiti.create.CreateMultiInstanceFeature;
import ru.runa.gpd.ui.graphiti.create.CreateReceiveMessageFeature;
import ru.runa.gpd.ui.graphiti.create.CreateSendMessageFeature;
import ru.runa.gpd.ui.graphiti.create.CreateSubProcessFeature;
import ru.runa.gpd.ui.graphiti.create.CreateTaskStateFeature;
import ru.runa.gpd.ui.graphiti.create.CreateStartNodeFeature;
import ru.runa.gpd.ui.graphiti.create.CreateTransitionFeature;
import ru.runa.gpd.ui.graphiti.delete.DeleteNodeFeature;
import ru.runa.gpd.ui.graphiti.edit.DirectEditNodeFeature;
import ru.runa.gpd.ui.graphiti.move.MoveNodeFeature;
import ru.runa.gpd.ui.graphiti.resize.ResizeNodeFeature;

public class DiagramFeatureProvider extends DefaultFeatureProvider {
    public IndependenceSolver independenceResolver;

    public DiagramFeatureProvider(IDiagramTypeProvider dtp) {
        super(dtp);
        setIndependenceSolver(new IndependenceSolver());
        independenceResolver = (IndependenceSolver) getIndependenceSolver();
    }

    public ProcessDefinition getCurrentProcessDefinition() {
        return ProcessCache.getProcessDefinition(ProjectFinder.getCurrentFile());
    }

    @Override
    public ICreateFeature[] getCreateFeatures() {
        return new ICreateFeature[] {
        		new CreateStartNodeFeature(this),
        		new CreateEndEventFeature(this),
        		new CreateTaskStateFeature(this),
        		new CreateDecisionFeature(this),
        		new CreateForkFeature(this),
        		new CreateJoinFeature(this),
        		new CreateSendMessageFeature(this),
        		new CreateReceiveMessageFeature(this),
        		new CreateSubProcessFeature(this),
        		new CreateMultiInstanceFeature(this)
        		};
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
    	if (context.getNewObject() instanceof Decision ||
    		context.getNewObject() instanceof Fork ||
    		context.getNewObject() instanceof Join ||
    		context.getNewObject() instanceof SendMessageNode ||
    		context.getNewObject() instanceof ReceiveMessageNode) {
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
        return new DeleteNodeFeature(this);
    }

    @Override
    public ICreateConnectionFeature[] getCreateConnectionFeatures() {
        return new ICreateConnectionFeature[] { new CreateTransitionFeature(this) };
    }

    @Override
    public IDirectEditingFeature getDirectEditingFeature(IDirectEditingContext context) {
        PictogramElement pe = context.getPictogramElement();
        Object bo = getBusinessObjectForPictogramElement(pe);
        if (bo instanceof Node) {
            return new DirectEditNodeFeature(this);
        }
        return null;
    }
    //    @Override
    //    public IUpdateFeature getUpdateFeature(IUpdateContext context) {
    //        PictogramElement pictogramElement = context.getPictogramElement();
    //        Object bo = getBusinessObjectForPictogramElement(pictogramElement);
    //        return super.getUpdateFeature(context);
    //    }
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
    //
    //    @Override
    //    public IReconnectionFeature getReconnectionFeature(IReconnectionContext context) {
    //        return new ReconnectSequenceFlowFeature(this);
    //    }
    //
    //
    //
    //    //
    //
    //
    //    @Override
    //    public ICustomFeature[] getCustomFeatures(ICustomContext context) {
    //        return new ICustomFeature[] { new SaveBpmnModelFeature(this), new DeleteSequenceFlowFeature(this), new DeletePoolFeature(this),
    //                new ChangeElementTypeFeature(this), new DeleteAssociationFeature(this) };
    //    }
}

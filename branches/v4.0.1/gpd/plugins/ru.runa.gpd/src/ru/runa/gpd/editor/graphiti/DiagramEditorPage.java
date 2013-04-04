package ru.runa.gpd.editor.graphiti;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.graphiti.add.AddTransitionFeature;
import ru.runa.gpd.editor.graphiti.update.BOUpdateContext;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;

public class DiagramEditorPage extends DiagramEditor implements PropertyChangeListener {
    private final ProcessEditorBase editor;

    public DiagramEditorPage(ProcessEditorBase editor) {
        this.editor = editor;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        //getSite().setSelectionProvider(editor.getSite().getSelectionProvider());
        editor.getDefinition().setDelegatedListener(this);
    }

    public ProcessDefinition getDefinition() {
        return editor.getDefinition();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        PictogramElement pe = getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(evt.getSource());
        if (pe != null) {
            BOUpdateContext context = new BOUpdateContext(pe, evt.getSource());
            getDiagramTypeProvider().getFeatureProvider().updateIfPossibleAndNeeded(context);
        }
    }

    @Override
    public void dispose() {
        editor.getDefinition().unsetDelegatedListener(this);
        super.dispose();
    }

    @Override
    protected void setInput(IEditorInput input) {
        DiagramCreator creator = new DiagramCreator(editor.getDefinitionFile());
        input = creator.createDiagram(null);
        super.setInput(input);
        importDiagram();
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        // hides grid on diagram, but you can re-enable it
        if (getGraphicalViewer() != null && getGraphicalViewer().getEditPartRegistry() != null) {
            ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) getGraphicalViewer().getEditPartRegistry().get(LayerManager.ID);
            IFigure gridFigure = ((LayerManager) rootEditPart).getLayer(LayerConstants.GRID_LAYER);
            gridFigure.setVisible(false);
        }
    }

    @Override
    protected ContextMenuProvider createContextMenuProvider() {
        return new DiagramContextMenuProvider(getGraphicalViewer(), getActionRegistry(), getDiagramTypeProvider());
    }

    //    @Override
    //    protected void hookGraphicalViewer() {
    //        getSelectionSynchronizer().addViewer(getGraphicalViewer());
    //        getSite().setSelectionProvider(new DelegableSelectionProvider(this, getGraphicalViewer()));
    //    }
    // translate selection
    //    @Override
    //    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    //        super.selectionChanged(part, selection);
    //        if (selection instanceof IStructuredSelection) {
    //            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    //            Object selected = structuredSelection.getFirstElement();
    //            if (!(selected instanceof EditPart)) {
    //                return;
    //            }
    //            if (structuredSelection.size() > 1) {
    //                return;
    //            }
    //            EditPart source = (EditPart) selected;
    //            if (source.getModel() instanceof PictogramElement) {
    //                PictogramElement pe = (PictogramElement) source.getModel();
    //                Object bo = getDiagramTypeProvider().getFeatureProvider().getBusinessObjectForPictogramElement(pe);
    //                getSite().getSelectionProvider().setSelection(new StructuredSelection(bo));
    //            }
    //        }
    //    }
    //    @Override
    //    public boolean isDirty() {
    //        TransactionalEditingDomain editingDomain = getEditingDomain();
    //        // Check that the editor is not yet disposed
    //        if (editingDomain != null && editingDomain.getCommandStack() != null) {
    //            return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
    //        }
    //        return false;
    //    }
    @Override
    public boolean isDirty() {
        return getCommandStack().isDirty();
    }

    private void importDiagram() {
        final Diagram diagram = getDiagramTypeProvider().getDiagram();
        getEditingDomain().getCommandStack().execute(new RecordingCommand(getEditingDomain()) {
            @Override
            protected void doExecute() {
                getDiagramTypeProvider().getFeatureProvider().link(diagram, editor.getDefinition());
                drawElements(diagram);
                drawTransitions();
            }
        });
    }

    public void select(GraphElement model) {
        PictogramElement pe = getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(model);
        selectPictogramElements(new PictogramElement[] { pe });
    }

    @Override
    public CommandStack getCommandStack() {
        return super.getCommandStack();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        //        IDiagramTypeProvider diagramTypeProvider = this.getDiagramTypeProvider();
        //        try {
        //            String diagramFileString = modelFile.getLocationURI().getPath();
        //            BPMN20ExportMarshaller marshaller = new BPMN20ExportMarshaller();
        //            marshaller.setSaveImage(true);
        //            marshaller.marshallDiagram(ModelHandler.getModel(EcoreUtil.getURI(getDiagramTypeProvider().getDiagram())), diagramFileString, diagramTypeProvider.getFeatureProvider());
        //            modelFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        //        ((BasicCommandStack) getEditingDomain().getCommandStack()).saveIsDone();
        //        updateDirtyState();
    }

    private void drawElements(ContainerShape parentShape) {
        List<GraphElement> graphElements = getDefinition().getContainerElements(getDefinition());
        drawElements(parentShape, graphElements);
    }

    private void drawElements(ContainerShape parentShape, List<? extends GraphElement> graphElements) {
        IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
        for (GraphElement graphElement : graphElements) {
            if (graphElement.getConstraint() == null) {
                continue;
            }
            AddContext context = new AddContext(new AreaContext(), graphElement);
            IAddFeature addFeature = featureProvider.getAddFeature(context);
            if (addFeature == null) {
                System.out.println("Element not supported: " + graphElement);
                continue;
            }
            context.setNewObject(graphElement);
            context.setTargetContainer(parentShape);
            context.setSize(graphElement.getConstraint().width, graphElement.getConstraint().height);
            context.setLocation(graphElement.getConstraint().x, graphElement.getConstraint().y);
            if (addFeature.canAdd(context)) {
                PictogramElement childContainer = addFeature.add(context);
                List<GraphElement> children = getDefinition().getContainerElements(graphElement);
                if (childContainer instanceof ContainerShape && children.size() > 0) {
                    drawElements((ContainerShape) childContainer, children);
                }
            }
        }
    }

    private void drawTransitions() {
        for (Transition transition : editor.getDefinition().getChildrenRecursive(Transition.class)) {
            Anchor sourceAnchor = null;
            Anchor targetAnchor = null;
            AnchorContainer sourceShape = (AnchorContainer) getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(transition.getSource());
            if (sourceShape == null) {
                continue;
            }
            EList<Anchor> anchorList = sourceShape.getAnchors();
            for (Anchor anchor : anchorList) {
                if (anchor instanceof ChopboxAnchor) {
                    sourceAnchor = anchor;
                    break;
                }
            }
            AnchorContainer targetShape = (AnchorContainer) getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(transition.getTarget());
            if (targetShape == null) {
                continue;
            }
            anchorList = targetShape.getAnchors();
            for (Anchor anchor : anchorList) {
                if (anchor instanceof ChopboxAnchor) {
                    targetAnchor = anchor;
                    break;
                }
            }
            AddConnectionContext addContext = new AddConnectionContext(sourceAnchor, targetAnchor);
            addContext.putProperty(AddTransitionFeature.BENDPOINTS_PROPERTY, transition.getBendpoints());
            addContext.setNewObject(transition);
            getDiagramTypeProvider().getFeatureProvider().addIfPossible(addContext);
        }
    }
}

package ru.runa.gpd.editor.graphiti;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
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
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class DiagramEditorPage extends DiagramEditor {
    private final ProcessEditorBase editor;

    public DiagramEditorPage(ProcessEditorBase editor) {
        this.editor = editor;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        //getSite().setSelectionProvider(editor.getSite().getSelectionProvider());
    }

    @Override
    protected void setInput(IEditorInput input) {
        DiagramCreator creator = new DiagramCreator(editor.getDefinitionFile());
        input = creator.createDiagram(null);
        super.setInput(input);
        //        if (input instanceof DiagramEditorInput) {
        //            BasicCommandStack basicCommandStack = (BasicCommandStack) getEditingDomain().getCommandStack();
        //            basicCommandStack.execute(new RecordingCommand(getEditingDomain()) {
        //                @Override
        //                protected void doExecute() {
        //                    importDiagram();
        //                }
        //            });
        //            basicCommandStack.saveIsDone();
        //            basicCommandStack.flush();
        //        } else {
        //            
        //        }
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
        //diagram.setActive(true);
        getEditingDomain().getCommandStack().execute(new RecordingCommand(getEditingDomain()) {
            @Override
            protected void doExecute() {
                drawFlowElements(editor.getDefinition().getChildren(Node.class), diagram);
                //drawSequenceFlows(model.getProcesses());
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

    //
    //    private PictogramElement addContainerElement(BaseElement element, ContainerShape parent) {
    //        final IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
    //        AddContext context = new AddContext(new AreaContext(), element);
    //        IAddFeature addFeature = featureProvider.getAddFeature(context);
    //        context.setNewObject(element);
    //        context.setSize(definition.getConstraint().width, definition.getConstraint().height);
    //        context.setTargetContainer(parent);
    //        int x = definition.getConstraint().x;
    //        int y = definition.getConstraint().y;
    //        if (parent instanceof Diagram == false) {
    //            x = x - parent.getGraphicsAlgorithm().getX();
    //            y = y - parent.getGraphicsAlgorithm().getY();
    //        }
    //        context.setLocation(x, y);
    //        PictogramElement pictElement = null;
    //        if (addFeature.canAdd(context)) {
    //            pictElement = addFeature.add(context);
    //            featureProvider.link(pictElement, new Object[] { element });
    //        }
    //        return pictElement;
    //    }
    private void drawFlowElements(List<Node> elementList, ContainerShape parentShape) {
        final IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
        for (Node node : elementList) {
            AddContext context = new AddContext(new AreaContext(), node);
            IAddFeature addFeature = featureProvider.getAddFeature(context);
            if (addFeature == null) {
                System.out.println("Element not supported: " + node);
                return;
            }
            context.setNewObject(node);
            context.setSize(node.getConstraint().width, node.getConstraint().height);
            context.setTargetContainer(parentShape);
            if (parentShape instanceof Diagram) {
                context.setLocation(node.getConstraint().x, node.getConstraint().y);
            } else {
                Point location = getLocation(parentShape);
                context.setLocation(node.getConstraint().x - location.x, node.getConstraint().y - location.y);
            }
            if (addFeature.canAdd(context)) {
                PictogramElement newContainer = addFeature.add(context);
                featureProvider.link(newContainer, new Object[] { node });
                //                    if (node instanceof SubProcess) {
                //                        drawFlowElements(((SubProcess) node).getFlowElements(), locationMap, (ContainerShape) newContainer, process);
                //                    }
                //                if (node instanceof Activity) {
                //                    Activity activity = (Activity) node;
                //                    for (BoundaryEvent boundaryEvent : activity.getBoundaryEvents()) {
                //                        AddContext boundaryContext = new AddContext(new AreaContext(), boundaryEvent);
                //                        IAddFeature boundaryAddFeature = featureProvider.getAddFeature(boundaryContext);
                //                        if (boundaryAddFeature == null) {
                //                            System.out.println("Element not supported: " + boundaryEvent);
                //                            return;
                //                        }
                //                        context.setNewObject(boundaryEvent);
                //                        context.setSize(node.getConstraint().width, node.getConstraint().height);
                //                        if (boundaryEvent.getAttachedToRef() != null) {
                //                            ContainerShape container = (ContainerShape) featureProvider.getPictogramElementForBusinessObject(boundaryEvent
                //                                    .getAttachedToRef());
                //                            if (container != null) {
                //                                boundaryContext.setTargetContainer(container);
                //                                Point location = getLocation(container);
                //                                boundaryContext.setLocation(node.getConstraint().x - location.x, node.getConstraint().y - location.y);
                //                                if (boundaryAddFeature.canAdd(boundaryContext)) {
                //                                    PictogramElement newBoundaryContainer = boundaryAddFeature.add(boundaryContext);
                //                                    featureProvider.link(newBoundaryContainer, new Object[] { boundaryEvent });
                //                                }
                //                            }
                //                        }
                //                    }
                //                }
            }
        }
    }

    private Point getLocation(ContainerShape containerShape) {
        if (containerShape instanceof Diagram == true) {
            return new Point(containerShape.getGraphicsAlgorithm().getX(), containerShape.getGraphicsAlgorithm().getY());
        }
        Point location = getLocation(containerShape.getContainer());
        return new Point(location.x + containerShape.getGraphicsAlgorithm().getX(), location.y + containerShape.getGraphicsAlgorithm().getY());
    }

    private void drawTransitions() {
        for (Transition transition : editor.getDefinition().getChildrenRecursive(Transition.class)) {
            Anchor sourceAnchor = null;
            Anchor targetAnchor = null;
            ContainerShape sourceShape = (ContainerShape) getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(transition.getSource());
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
            ContainerShape targetShape = (ContainerShape) getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(transition.getTarget());
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

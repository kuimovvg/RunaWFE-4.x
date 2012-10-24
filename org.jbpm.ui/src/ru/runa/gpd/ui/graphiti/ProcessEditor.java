package ru.runa.gpd.ui.graphiti;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
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
import org.eclipse.graphiti.ui.editor.DiagramEditorContextMenuProvider;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.jbpm.ui.ProcessCache;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Transition;

public class ProcessEditor extends DiagramEditor {
    public final static String ID = "ru.runa.wfe.graphitiEditor";
    private static GraphicalViewer activeGraphicalViewer;
    private ProcessDefinition definition;
    private IFile definitionFile;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (!(input instanceof FileEditorInput)) {
            throw new PartInitException("Reopen from file is not supported now");
        }
        FileEditorInput fileInput = (FileEditorInput) input;
        this.definitionFile = fileInput.getFile();
        IPath path = fileInput.getPath().removeLastSegments(1);
        path = path.removeFirstSegments(path.segmentCount() - 1);
        setPartName(path.lastSegment());
        definition = ProcessCache.getProcessDefinition(definitionFile);
        definition.setDirty(false);
        // change editor input?
        DiagramCreator creator = new DiagramCreator(definitionFile);
        input = creator.createDiagram(false, null);
        super.init(site, input);
        getSite().getPage().addSelectionListener(this);
        // ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        // definition.addPropertyChangeListener(this);
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        BasicCommandStack basicCommandStack = (BasicCommandStack) getEditingDomain().getCommandStack();
        if (input instanceof DiagramEditorInput) {
            basicCommandStack.execute(new RecordingCommand(getEditingDomain()) {
                @Override
                protected void doExecute() {
                    importDiagram();
                }
            });
        }
        basicCommandStack.saveIsDone();
        basicCommandStack.flush();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        //        IDiagramTypeProvider diagramTypeProvider = this.getDiagramTypeProvider();
        //        try {
        //            String diagramFileString = modelFile.getLocationURI().getPath();
        //            boolean saveImage = PreferencesUtil.getBooleanPreference(Preferences.SAVE_IMAGE);
        //            BPMN20ExportMarshaller marshaller = new BPMN20ExportMarshaller();
        //            marshaller.setSaveImage(saveImage);
        //            marshaller.marshallDiagram(ModelHandler.getModel(EcoreUtil.getURI(getDiagramTypeProvider().getDiagram())), diagramFileString,
        //                    diagramTypeProvider.getFeatureProvider());
        //            modelFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        //        ((BasicCommandStack) getEditingDomain().getCommandStack()).saveIsDone();
        //        updateDirtyState();
    }

    @Override
    public boolean isDirty() {
        TransactionalEditingDomain editingDomain = getEditingDomain();
        // Check that the editor is not yet disposed
        if (editingDomain != null && editingDomain.getCommandStack() != null) {
            return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
        }
        return false;
    }

    private void importDiagram() {
        final Diagram diagram = getDiagramTypeProvider().getDiagram();
        diagram.setActive(true);
        getEditingDomain().getCommandStack().execute(new RecordingCommand(getEditingDomain()) {
            @Override
            protected void doExecute() {
                //                    for (Pool pool : model.getPools()) {
                //                        PictogramElement poolElement = addContainerElement(pool, model, diagram);
                //                        if (poolElement == null)
                //                            continue;
                //                        Process process = model.getProcess(pool.getId());
                //                        for (Lane lane : process.getLanes()) {
                //                            addContainerElement(lane, model, (ContainerShape) poolElement);
                //                        }
                //                    }
                drawFlowElements(definition.getChildren(Node.class), diagram);
                //                    drawArtifacts(process.getArtifacts(), model.getLocationMap(), diagram, process);
                //drawSequenceFlows(model.getProcesses());
                drawAssociations();
            }
        });
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
            ContainerShape parentContainer = null;
            if (parentShape instanceof Diagram) {
                parentContainer = getParentContainer(node.getName(), (Diagram) parentShape);
            } else {
                parentContainer = parentShape;
            }
            context.setTargetContainer(parentContainer);
            if (parentContainer instanceof Diagram == false) {
                Point location = getLocation(parentContainer);
                context.setLocation(node.getConstraint().x - location.x, node.getConstraint().y - location.y);
            } else {
                context.setLocation(node.getConstraint().x, node.getConstraint().y);
            }
            //            if (node instanceof ServiceTask) {
            //                // Customize the name displayed by default
            //                final List<CustomServiceTask> customServiceTasks = ExtensionUtil.getCustomServiceTasks(ActivitiUiUtil
            //                        .getProjectFromDiagram(getDiagramTypeProvider().getDiagram()));
            //                ServiceTask serviceTask = (ServiceTask) node;
            //                CustomServiceTask targetTask = null;
            //                for (final CustomServiceTask customServiceTask : customServiceTasks) {
            //                    if (customServiceTask.getRuntimeClassname().equals(serviceTask.getImplementation())) {
            //                        targetTask = customServiceTask;
            //                        break;
            //                    }
            //                }
            //                if (targetTask != null) {
            //                    CustomProperty customServiceTaskProperty = new CustomProperty();
            //                    customServiceTaskProperty.setId(ExtensionUtil.wrapCustomPropertyId(serviceTask,
            //                            ExtensionConstants.PROPERTY_ID_CUSTOM_SERVICE_TASK));
            //                    customServiceTaskProperty.setName(ExtensionConstants.PROPERTY_ID_CUSTOM_SERVICE_TASK);
            //                    customServiceTaskProperty.setSimpleValue(targetTask.getId());
            //                    serviceTask.getCustomProperties().add(customServiceTaskProperty);
            //                    for (FieldExtension field : serviceTask.getFieldExtensions()) {
            //                        CustomProperty customFieldProperty = new CustomProperty();
            //                        customFieldProperty.setName(field.getFieldName());
            //                        customFieldProperty.setSimpleValue(field.getExpression());
            //                        serviceTask.getCustomProperties().add(customFieldProperty);
            //                    }
            //                    serviceTask.getFieldExtensions().clear();
            //                }
            //            }
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

    private ContainerShape getParentContainer(String flowElementId, Diagram diagram) {
        //        Lane foundLane = null;
        //        for (Lane lane : process.getLanes()) {
        //            if (lane.getFlowReferences().contains(flowElementId)) {
        //                foundLane = lane;
        //                break;
        //            }
        //        }
        //        if (foundLane != null) {
        //            final IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
        //            return (ContainerShape) featureProvider.getPictogramElementForBusinessObject(foundLane);
        //        } else {
        return diagram;
        //        }
    }

    private Point getLocation(ContainerShape containerShape) {
        if (containerShape instanceof Diagram == true) {
            return new Point(containerShape.getGraphicsAlgorithm().getX(), containerShape.getGraphicsAlgorithm().getY());
        }
        Point location = getLocation(containerShape.getContainer());
        return new Point(location.x + containerShape.getGraphicsAlgorithm().getX(), location.y + containerShape.getGraphicsAlgorithm().getY());
    }

    //    private void drawArtifacts(final List<Artifact> artifacts, final Map<String, GraphicInfo> locationMap, final ContainerShape parent,
    //            final Process process) {
    //        final IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
    //        final List<Artifact> artifactsWithoutDI = new ArrayList<Artifact>();
    //        for (final Artifact artifact : artifacts) {
    //            final AddContext context = new AddContext(new AreaContext(), artifact);
    //            final IAddFeature addFeature = featureProvider.getAddFeature(context);
    //            if (addFeature == null) {
    //                System.out.println("Element not supported: " + artifact);
    //                return;
    //            }
    //            final GraphicInfo gi = locationMap.get(artifact.getId());
    //            if (gi == null) {
    //                artifactsWithoutDI.add(artifact);
    //            } else {
    //                context.setNewObject(artifact);
    //                context.setSize(gi.width, gi.height);
    //                ContainerShape parentContainer = null;
    //                if (parent instanceof Diagram) {
    //                    parentContainer = getParentContainer(artifact.getId(), process, (Diagram) parent);
    //                } else {
    //                    parentContainer = parent;
    //                }
    //                context.setTargetContainer(parentContainer);
    //                if (parentContainer instanceof Diagram) {
    //                    context.setLocation(gi.x, gi.y);
    //                } else {
    //                    final Point location = getLocation(parentContainer);
    //                    context.setLocation(gi.x - location.x, gi.y - location.y);
    //                }
    //                if (addFeature.canAdd(context)) {
    //                    final PictogramElement newContainer = addFeature.add(context);
    //                    featureProvider.link(newContainer, new Object[] { artifact });
    //                }
    //            }
    //        }
    //        for (final Artifact artifact : artifactsWithoutDI) {
    //            artifacts.remove(artifact);
    //        }
    //    }
    //    private void drawSequenceFlows(List<Process> processes) {
    //        int sequenceCounter = 1;
    //        for (SequenceFlowModel sequenceFlowModel : parser.sequenceFlowList) {
    //            SequenceFlow sequenceFlow = new SequenceFlow();
    //            if (Strings.isNullOrEmpty(sequenceFlowModel.id) || sequenceFlowModel.id.matches("sid-\\w{4,12}-\\w{4,12}-\\w{4,12}-\\w{4,12}-\\w{4,12}")) {
    //                sequenceFlow.setId("flow" + sequenceCounter);
    //                sequenceCounter++;
    //            } else {
    //                sequenceFlow.setId(sequenceFlowModel.id);
    //            }
    //            if (Strings.isNullOrEmpty(sequenceFlowModel.name)) {
    //                sequenceFlow.setName(sequenceFlowModel.name);
    //            }
    //            sequenceFlow.setSourceRef(getFlowNode(sequenceFlowModel.sourceRef, processes));
    //            sequenceFlow.setTargetRef(getFlowNode(sequenceFlowModel.targetRef, processes));
    //            if (sequenceFlow.getSourceRef() == null || sequenceFlow.getSourceRef().getId() == null || sequenceFlow.getTargetRef() == null
    //                    || sequenceFlow.getTargetRef().getId() == null)
    //                continue;
    //            if (sequenceFlowModel.conditionExpression != null) {
    //                sequenceFlow.setConditionExpression(sequenceFlowModel.conditionExpression);
    //            }
    //            if (sequenceFlowModel.listenerList.size() > 0) {
    //                sequenceFlow.getExecutionListeners().addAll(sequenceFlowModel.listenerList);
    //            }
    //            SubProcess subProcessContainsFlow = null;
    //            for (FlowElement flowElement : sequenceFlowModel.parentProcess.getFlowElements()) {
    //                if (flowElement instanceof SubProcess) {
    //                    SubProcess subProcess = (SubProcess) flowElement;
    //                    if (subProcess.getFlowElements().contains(sequenceFlow.getSourceRef())) {
    //                        subProcessContainsFlow = subProcess;
    //                    }
    //                }
    //            }
    //            if (subProcessContainsFlow != null) {
    //                subProcessContainsFlow.getFlowElements().add(sequenceFlow);
    //            } else {
    //                sequenceFlowModel.parentProcess.getFlowElements().add(sequenceFlow);
    //            }
    //            sequenceFlow.getSourceRef().getOutgoing().add(sequenceFlow);
    //            sequenceFlow.getTargetRef().getIncoming().add(sequenceFlow);
    //            Anchor sourceAnchor = null;
    //            Anchor targetAnchor = null;
    //            ContainerShape sourceShape = (ContainerShape) getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(
    //                    sequenceFlow.getSourceRef());
    //            if (sourceShape == null)
    //                continue;
    //            EList<Anchor> anchorList = sourceShape.getAnchors();
    //            for (Anchor anchor : anchorList) {
    //                if (anchor instanceof ChopboxAnchor) {
    //                    sourceAnchor = anchor;
    //                    break;
    //                }
    //            }
    //            ContainerShape targetShape = (ContainerShape) getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(
    //                    sequenceFlow.getTargetRef());
    //            if (targetShape == null)
    //                continue;
    //            anchorList = targetShape.getAnchors();
    //            for (Anchor anchor : anchorList) {
    //                if (anchor instanceof ChopboxAnchor) {
    //                    targetAnchor = anchor;
    //                    break;
    //                }
    //            }
    //            AddConnectionContext addContext = new AddConnectionContext(sourceAnchor, targetAnchor);
    //            List<GraphicInfo> bendpointList = new ArrayList<GraphicInfo>();
    //            if (parser.flowLocationMap.containsKey(sequenceFlowModel.id)) {
    //                List<GraphicInfo> pointList = parser.flowLocationMap.get(sequenceFlowModel.id);
    //                if (pointList.size() > 2) {
    //                    for (int i = 1; i < pointList.size() - 1; i++) {
    //                        bendpointList.add(pointList.get(i));
    //                    }
    //                }
    //            }
    //            addContext.putProperty("org.activiti.designer.bendpoints", bendpointList);
    //            addContext.putProperty("org.activiti.designer.connectionlabel", parser.labelLocationMap.get(sequenceFlowModel.id));
    //            addContext.setNewObject(sequenceFlow);
    //            getDiagramTypeProvider().getFeatureProvider().addIfPossible(addContext);
    //        }
    //    }
    private void drawAssociations() {
        for (Transition transition : definition.getChildrenRecursive(Transition.class)) {
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
            addContext.putProperty("org.activiti.designer.bendpoints", transition.getBendpoints());
            addContext.setNewObject(transition);
            getDiagramTypeProvider().getFeatureProvider().addIfPossible(addContext);
        }
    }

    private Node getBaseElement(String elementId) {
        return definition.getNodeByNameNotNull(elementId);
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
        // setPartName("MyDiagram2");
    }

    @Override
    protected ContextMenuProvider createContextMenuProvider() {
        return new DiagramEditorContextMenuProvider(getGraphicalViewer(), getActionRegistry(), getDiagramTypeProvider());
    }

    public static GraphicalViewer getActiveGraphicalViewer() {
        return activeGraphicalViewer;
    }
}

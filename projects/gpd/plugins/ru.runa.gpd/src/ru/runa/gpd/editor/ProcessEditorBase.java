package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.editor.gef.GEFImageHelper;
import ru.runa.gpd.editor.gef.GEFProcessEditor;
import ru.runa.gpd.editor.gef.part.graph.ElementGraphicalEditPart;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;
import ru.runa.gpd.ui.view.ValidationErrorsView;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;

public abstract class ProcessEditorBase extends MultiPageEditorPart implements ISelectionListener, IResourceChangeListener, PropertyChangeListener {
    protected ProcessDefinition definition;
    protected IFile definitionFile;
    protected GraphicalEditor graphPage;
    protected SwimlaneEditorPage swimlanePage;
    protected VariableEditorPage variablePage;
    protected VariableTypeEditorPage variableTypeEditorPage;
    protected TextEditor sourcePage;
    private OutlineViewer outlineViewer;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        getSite().getPage().addSelectionListener(this);
        FileEditorInput fileInput = (FileEditorInput) input;
        this.definitionFile = fileInput.getFile();
        IPath path = fileInput.getPath().removeLastSegments(1);
        path = path.removeFirstSegments(path.segmentCount() - 1);
        definition = ProcessCache.getProcessDefinition(definitionFile);
        definition.setDirty(false);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        definition.addPropertyChangeListener(this);
        setPartName(definition.getName());
    }

    @Override
    public void dispose() {
        try {
            if (definition != null) {
                definition.removePropertyChangeListener(this);
                // If process definition is dirty (hasn't been saved) we should
                // reload it from XML
                if (definition.isDirty()) {
                    ProcessCache.invalidateProcessDefinition(definitionFile);
                }
            }
            if (definitionFile != null && definitionFile.exists()) {
                definitionFile.deleteMarkers(ValidationErrorsView.ID, true, IResource.DEPTH_INFINITE);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        getSite().getPage().removeSelectionListener(this);
        super.dispose();
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        EditorUtils.closeEditorIfRequired(event, definitionFile, this);
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        try {
            if (!(selection instanceof IStructuredSelection)) {
                return;
            }
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object selectedObject = structuredSelection.getFirstElement();
            if (selectedObject == null) {
                return;
            }
            String activateViewId = null;
            if (selectedObject instanceof ElementGraphicalEditPart) {
                activateViewId = ((ElementGraphicalEditPart) selectedObject).getAssociatedViewId();
            }
            if (part instanceof GEFProcessEditor) {
                // select only variables and swimlanes
                if (selectedObject instanceof Swimlane || selectedObject instanceof Variable) {
                    selectGraphElement((GraphElement) selectedObject);
                }
            }
            if (part instanceof ContentOutline) {
                Object model = ((EditPart) selectedObject).getModel();
                if (!(model instanceof GraphElement)) {
                    return;
                }
                select((GraphElement) model);
            }
            if (activateViewId != null) {
                IViewPart viewPart = getSite().getPage().findView(activateViewId);
                if (!getSite().getPage().isPartVisible(viewPart)) {
                    try {
                        getSite().getPage().showView(activateViewId, null, IWorkbenchPage.VIEW_VISIBLE);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("DesignerEditor.selectionChanged", e);
        }
    }

    @Override
    protected void createPages() {
        try {
            int pageIndex;
            graphPage = createGraphPage();
            pageIndex = addPage(graphPage, getEditorInput());
            setPageText(pageIndex, Localization.getString("DesignerEditor.title.diagram"));
            if (!(definition instanceof SubprocessDefinition)) {
                swimlanePage = new SwimlaneEditorPage(this);
                pageIndex = addPage(swimlanePage, getEditorInput());
                setPageText(pageIndex, Localization.getString("DesignerEditor.title.swimlanes"));
                variablePage = new VariableEditorPage(this);
                pageIndex = addPage(variablePage, getEditorInput());
                setPageText(pageIndex, Localization.getString("DesignerEditor.title.variables"));
                variableTypeEditorPage = new VariableTypeEditorPage(this);
                pageIndex = addPage(variableTypeEditorPage, getEditorInput());
                setPageText(pageIndex, Localization.getString("VariableUserType.collection"));
            }
            sourcePage = new TextEditor();
            pageIndex = addPage(sourcePage, getEditorInput());
            setPageText(pageIndex, Localization.getString("DesignerEditor.title.source"));
            ProcessDefinitionValidator.validateDefinition(definitionFile, definition);
        } catch (PartInitException e) {
            PluginLogger.logError(Localization.getString("DesignerEditor.error.can_not_create_graphical_viewer"), e);
            throw new RuntimeException(e);
        }
    }

    public void select(GraphElement model) {
        if (model instanceof Swimlane && swimlanePage != null) {
            openPage(1);
            swimlanePage.select((Swimlane) model);
        } else if (model instanceof Variable && variablePage != null) {
            openPage(2);
            variablePage.select((Variable) model);
        } else {
            openPage(0);
            selectGraphElement(model);
        }
    }

    protected abstract GraphicalEditor createGraphPage();

    protected abstract void selectGraphElement(GraphElement model);

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IContentOutlinePage.class) {
            return getOutlineViewer();
        }
        if (adapter == ActionRegistry.class) {
            return graphPage.getAdapter(adapter);
        }
        return super.getAdapter(adapter);
    }

    public IFigure getRootFigure() {
        return (IFigure) graphPage.getAdapter(IFigure.class);
    }

    public GraphicalViewer getGraphicalViewer() {
        return (GraphicalViewer) graphPage.getAdapter(GraphicalViewer.class);
    }

    public CommandStack getCommandStack() {
        return (CommandStack) graphPage.getAdapter(CommandStack.class);
    }

    public EditDomain getEditDomain() {
        return getGraphicalViewer().getEditDomain();
    }

    public OutlineViewer getOutlineViewer() {
        if (outlineViewer == null && getGraphicalViewer() != null) {
            outlineViewer = new OutlineViewer(this);
        }
        return outlineViewer;
    }

    public void openPage(int number) {
        if (getActivePage() != number) {
            setActivePage(number);
            setFocus();
        }
    }

    public void refresh() {
        IFigure figure = getRootFigure();
        figure.revalidate();
        figure.repaint();
        figure.invalidateTree();
    }

    public ProcessDefinition getDefinition() {
        return definition;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyNames.PROPERTY_DIRTY.equals(evt.getPropertyName())) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
        if (PropertyNames.PROPERTY_SHOW_GRID.equals(evt.getPropertyName())) {
            getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, evt.getNewValue());
            getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, evt.getNewValue());
            refresh();
        }
    }

    private String getGraphImagePath() {
        IFile file = ((FileEditorInput) getEditorInput()).getFile();
        String fileName = ParContentProvider.PROCESS_IMAGE_FILE_NAME;
        if (definition instanceof SubprocessDefinition) {
            fileName = definition.getId() + "." + fileName;
        }
        IFile imageFile = IOUtils.getAdjacentFile(file, fileName);
        return imageFile.getRawLocation().toOSString();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        GEFImageHelper.save(getGraphicalViewer(), definition, getGraphImagePath());
        try {
            ProcessDefinitionValidator.validateDefinition(definitionFile, definition);
            WorkspaceOperations.saveProcessDefinition(definitionFile, definition);
            getCommandStack().markSaveLocation();
            sourcePage.setInput(sourcePage.getEditorInput());
            definition.setDirty(false);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
        ProcessDefinition mainProcessDefinition = definition;
        while (mainProcessDefinition instanceof SubprocessDefinition) {
            mainProcessDefinition = (ProcessDefinition) mainProcessDefinition.getParent();
        }
        try {
            Set<String> usedFormFiles = new HashSet<String>();
            fetchUsedFormFiles(usedFormFiles, mainProcessDefinition);
            for (SubprocessDefinition subprocessDefinition : mainProcessDefinition.getEmbeddedSubprocesses().values()) {
                fetchUsedFormFiles(usedFormFiles, subprocessDefinition);
            }
            IFolder folder = (IFolder) definitionFile.getParent();
            IResource[] children = folder.members(true);
            for (IResource resource : children) {
                boolean interested = IOUtils.looksLikeFormFile(resource.getName());
                if (interested && !usedFormFiles.contains(resource.getName())) {
                    resource.delete(true, null);
                }
            }
        } catch (CoreException e) {
            PluginLogger.logError("Cleaning unused form files", e);
        }
    }
    
    private void fetchUsedFormFiles(Set<String> usedFormFiles, ProcessDefinition processDefinition) {
        List<FormNode> formNodes = processDefinition.getChildren(FormNode.class);
        for (FormNode formNode : formNodes) {
            if (formNode.hasForm()) {
                usedFormFiles.add(formNode.getFormFileName());
            }
            if (formNode.hasFormValidation()) {
                usedFormFiles.add(formNode.getValidationFileName());
            }
        }
    }

    public IFile getDefinitionFile() {
        return definitionFile;
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return graphPage.isDirty() || definition.isDirty();
    }

    @Override
    public boolean isSaveOnCloseNeeded() {
        return isDirty();
    }

    @Override
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
    }
}

package ru.runa.bpm.ui.editor;

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
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
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
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.ProcessCache;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.NotificationMessages;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.Swimlane;
import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.common.part.graph.ElementGraphicalEditPart;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.EditorUtils;
import ru.runa.bpm.ui.util.IOUtils;
import ru.runa.bpm.ui.util.WorkspaceOperations;
import ru.runa.bpm.ui.view.ValidationErrorsView;

import tk.eclipse.plugin.xmleditor.editors.XMLEditor;

public class DesignerEditor extends MultiPageEditorPart implements ISelectionListener, IResourceChangeListener, PropertyChangeListener {

    public static final String ID = "ru.runa.bpm.ui.editor.DesignerEditor";

    private ProcessDefinition definition;
    private IFile definitionFile;

    private DesignerGraphicalEditorPart graphPage;
    private DesignerSwimlaneEditorPage swimlanePage;
    private DesignerVariableEditorPage variablePage;
    private XMLEditor sourcePage;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        getSite().getPage().addSelectionListener(this);
        FileEditorInput fileInput = (FileEditorInput) input;
        this.definitionFile = fileInput.getFile();
        IPath path = fileInput.getPath().removeLastSegments(1);
        path = path.removeFirstSegments(path.segmentCount() - 1);
        setPartName(path.lastSegment());
        definition = ProcessCache.getProcessDefinition(definitionFile);
        definition.setDirty(false);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        definition.addPropertyChangeListener(this);
    }

    @Override
    public FileEditorInput getEditorInput() {
        return (FileEditorInput) super.getEditorInput();
    }

    @Override
    public void dispose() {
        try {
            if (definition != null) {
                definition.removePropertyChangeListener(this);
            }
            // If process definition is dirty (hasn't been saved) we should
            // reload it from XML
            if (definition != null && definition.isDirty()) {
                ProcessCache.invalidateProcessDefinition(definitionFile);
            }
            if (definitionFile.exists()) {
                definitionFile.deleteMarkers(ValidationErrorsView.ID, true, IResource.DEPTH_INFINITE);
            }
        } catch (Exception e) {
            DesignerLogger.logError(e);
        }

        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        getSite().getPage().removeSelectionListener(this);
        super.dispose();
    }

    public void resourceChanged(IResourceChangeEvent event) {
        EditorUtils.closeEditorIfRequired(event, definitionFile, this);
    }

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
            if (part instanceof DesignerEditor) {
                // select only variables and swimlanes
                if (selectedObject instanceof Swimlane || selectedObject instanceof Variable) {
                    graphPage.select((GraphElement) selectedObject);
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
            DesignerLogger.logErrorWithoutDialog("DesignerEditor.selectionChanged", e);
        }
    }

    @Override
    protected void createPages() {
        try {
            graphPage = new DesignerGraphicalEditorPart(this);
            addPage(0, graphPage, getEditorInput());
            setPageText(0, Messages.getString("DesignerEditor.title.diagram"));

            swimlanePage = new DesignerSwimlaneEditorPage(this);
            addPage(1, swimlanePage, getEditorInput());
            setPageText(1, Messages.getString("DesignerEditor.title.swimlanes"));

            variablePage = new DesignerVariableEditorPage(this);
            addPage(2, variablePage, getEditorInput());
            setPageText(2, Messages.getString("DesignerEditor.title.variables"));

            sourcePage = new XMLEditor();
            addPage(3, sourcePage, getEditorInput());
            setPageText(3, Messages.getString("DesignerEditor.title.source"));

            definition.validateDefinition(definitionFile);
        } catch (PartInitException e) {
            DesignerLogger.logError(Messages.getString("DesignerEditor.error.can_not_create_graphical_viewer"), e);
            throw new RuntimeException(e);
        }
    }

    public void select(GraphElement model) {
        if (model instanceof Swimlane) {
            openPage(1);
            swimlanePage.select((Swimlane) model);
        } else if (model instanceof Variable) {
            openPage(2);
            variablePage.select((Variable) model);
        } else {
            openPage(0);
            graphPage.select(model);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        return graphPage.getAdapter(adapter);
    }

    public void openPage(int number) {
        if (getActivePage() != number) {
            setActivePage(number);
            setFocus();
        }
    }

    public void refresh() {
        IFigure figure = (IFigure) getAdapter(IFigure.class);
        figure.revalidate();
        figure.repaint();
        figure.invalidateTree();
    }

    public ProcessDefinition getDefinition() {
        return definition;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (NotificationMessages.PROPERTY_DIRTY.equals(evt.getPropertyName())) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
        if (NotificationMessages.PROPERTY_SHOW_ACTIONS.equals(evt.getPropertyName())) {
            graphPage.getPaletteRoot().refreshActionsVisibility();
        }
        if (NotificationMessages.PROPERTY_SHOW_GRID.equals(evt.getPropertyName())) {
            refresh();
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        graphPage.doSave(monitor);
        try {
            definition.validateDefinition(definitionFile);
            WorkspaceOperations.saveProcessDefinition(definitionFile, definition);
            graphPage.getCommandStack().markSaveLocation();
            sourcePage.reloadXML();
            definition.setDirty(false);
        } catch (Exception e) {
            DesignerLogger.logError(e);
        }
        try {
            Set<String> usedFormFiles = new HashSet<String>();
            usedFormFiles.add("index.html");
            List<FormNode> formNodes = definition.getChildren(FormNode.class);
            for (FormNode formNode : formNodes) {
                if (formNode.hasForm()) {
                    usedFormFiles.add(formNode.getFormFileName());
                }
                if (formNode.hasFormValidation()) {
                    usedFormFiles.add(formNode.getValidationFileName());
                }
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
            DesignerLogger.logError("Cleaning unused form files", e);
        }
    }

    public IFile getDefinitionFile() {
        return definitionFile;
    }

    public CommandStack getCommandStack() {
        return graphPage.getCommandStack();
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
        return getCommandStack().isDirty() || definition.isDirty();
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

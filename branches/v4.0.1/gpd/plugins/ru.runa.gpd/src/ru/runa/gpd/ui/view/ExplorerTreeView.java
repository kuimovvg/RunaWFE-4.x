package ru.runa.gpd.ui.view;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.ui.custom.LoggingDoubleClickListener;
import ru.runa.gpd.util.WorkspaceOperations;

public class ExplorerTreeView extends ViewPart implements ISelectionListener {
    private TreeViewer viewer;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
    }

    @Override
    public void dispose() {
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        super.dispose();
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof ProcessEditorBase) {
            IContainer definitionFolder = ((ProcessEditorBase) part).getDefinitionFile().getParent();
            IContainer projectFolder = definitionFolder.getParent().getParent().getParent();
            viewer.expandToLevel(projectFolder, AbstractTreeViewer.ALL_LEVELS);
            viewer.setSelection(new StructuredSelection(definitionFolder), true);
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.NONE);
        viewer.setContentProvider(new ResourcesContentProvider());
        viewer.setLabelProvider(new ResourcesLabelProvider());
        viewer.setInput(new Object());
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
            @Override
            public void resourceChanged(IResourceChangeEvent event) {
                try {
                    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (!viewer.getControl().isDisposed()) {
                                viewer.refresh();
                            }
                        }
                    });
                } catch (Exception e) {
                    // disposed
                }
            }
        });
        viewer.addDoubleClickListener(new LoggingDoubleClickListener() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (element instanceof IFolder) {
                    WorkspaceOperations.openProcessDefinition((IFolder) element);
                }
            }
        });
        getSite().setSelectionProvider(viewer);
        MenuManager menuMgr = new MenuManager();
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                ExplorerTreeView.this.fillContextMenu(manager);
            }
        });
        viewer.getControl().setMenu(menu);
    }

    @SuppressWarnings("unchecked")
    protected void fillContextMenu(IMenuManager manager) {
        final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        final Object selectedObject = selection.getFirstElement();
        final List<IResource> resources = selection.toList();
        boolean menuOnProject = selectedObject instanceof IProject;
        boolean menuOnProcess = selectedObject instanceof IFolder;
        //
        if (menuOnProcess) {
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.openProcess")) {
                @Override
                public void run() {
                    WorkspaceOperations.openProcessDefinition((IFolder) selectedObject);
                }
            });
        }
        manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.newProject"), SharedImages.getImageDescriptor("icons/add_project.gif")) {
            @Override
            public void run() {
                WorkspaceOperations.createNewProject();
            }
        });
        if (menuOnProject) {
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.newProcess"), SharedImages.getImageDescriptor("icons/process.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.createNewProcessDefinition(selection);
                }
            });
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.importProcess"), SharedImages.getImageDescriptor("icons/import.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.importProcessDefinition(selection);
                }
            });
        }
        if (menuOnProcess) {
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.copyProcess"), SharedImages.getImageDescriptor("icons/copy.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.copyProcessDefinition(selection);
                }
            });
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.exportProcess"), SharedImages.getImageDescriptor("icons/export.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.exportProcessDefinition(selection);
                }
            });
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.renameProcess"), SharedImages.getImageDescriptor("icons/rename.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.renameProcessDefinition(selection);
                }
            });
        }
        if (menuOnProject || menuOnProcess) {
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.refresh"), SharedImages.getImageDescriptor("icons/refresh.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.refreshResources(resources);
                }
            });
            manager.add(new Action(Localization.getString("button.delete"), SharedImages.getImageDescriptor("icons/delete.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.deleteResources(resources);
                }
            });
        }
    }

    @Override
    public void setFocus() {
    }
}

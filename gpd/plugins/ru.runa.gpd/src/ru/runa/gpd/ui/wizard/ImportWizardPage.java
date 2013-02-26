package ru.runa.gpd.ui.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.EditPart;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.ProjectFinder;

public abstract class ImportWizardPage extends WizardPage {
    protected final IProject project;
    protected ListViewer projectViewer;

    public ImportWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName);
        this.project = getInitialJavaElement(selection);
    }

    private IProject getInitialJavaElement(IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof EditPart) {
                return ProjectFinder.getCurrentProject();
            }
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;
                IResource resource = (IResource) adaptable.getAdapter(IResource.class);
                if (resource != null) {
                    return resource.getProject();
                }
                IJavaElement javaElement = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
                if (javaElement != null) {
                    return javaElement.getJavaProject().getProject();
                }
            }
        }
        return null;
    }

    protected void createProjectsGroup(Composite parent) {
        Group projectListGroup = new Group(parent, SWT.NONE);
        projectListGroup.setLayout(new GridLayout(1, false));
        projectListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        projectListGroup.setText(Localization.getString("label.project"));
        createProjectsList(projectListGroup);
    }

    private void createProjectsList(Composite parent) {
        projectViewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        projectViewer.getControl().setLayoutData(gridData);
        projectViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((IProject) element).getName();
            }
        });
        projectViewer.setContentProvider(new ArrayContentProvider());
        projectViewer.setInput(ProjectFinder.getAllProcessDefinitionProjects());
        if (project != null) {
            projectViewer.setSelection(new StructuredSelection(project));
        }
    }

    protected IProject getSelectedProject() throws Exception {
        IStructuredSelection selectedProject = (IStructuredSelection) projectViewer.getSelection();
        IProject project = (IProject) selectedProject.getFirstElement();
        if (project == null) {
            throw new Exception(Localization.getString("ImportParWizardPage.error.selectTargetProject"));
        }
        return project;
    }
}

package ru.runa.gpd.ui.wizard;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.EditPart;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.util.ProjectFinder;
import ru.runa.gpd.util.SwimlaneDisplayMode;

public class NewProcessDefinitionWizardPage extends WizardPage {
    private Combo projectCombo;
    private Text processText;
    private Combo languageCombo;
    private Combo bpmnDisplaySwimlaneCombo;
    private final IWorkspaceRoot workspaceRoot;
    private final IStructuredSelection selection;

    public NewProcessDefinitionWizardPage(IStructuredSelection selection) {
        super(Localization.getString("NewProcessDefinitionWizardPage.page.name"));
        this.selection = selection;
        this.workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        setTitle(Localization.getString("NewProcessDefinitionWizardPage.page.title"));
        setDescription(Localization.getString("NewProcessDefinitionWizardPage.page.description"));
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        composite.setLayout(layout);
        createProjectField(composite);
        createProcessNameField(composite);
        createLanguageCombo(composite);
        createBpmnDisplaySwimlaneCombo(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        setPageComplete(false);
        processText.setFocus();
    }

    private void createProjectField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.project"));
        projectCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (IProject project : ProjectFinder.getAllProcessDefinitionProjects()) {
            projectCombo.add(project.getName());
        }
        projectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        IProject project = getInitialJavaElement(selection);
        if (project != null) {
            projectCombo.setText(project.getName());
        }
        projectCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                verifyContentsValid();
            }
        });
    }

    private void createProcessNameField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.process_name"));
        processText = new Text(parent, SWT.BORDER);
        processText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        processText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createLanguageCombo(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.language"));
        languageCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (Language language : Language.values()) {
            languageCombo.add(language.name());
        }
        languageCombo.select(0);
        languageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        languageCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = languageCombo.getSelectionIndex() == 1;
                bpmnDisplaySwimlaneCombo.setEnabled(enabled);
                if (!enabled) {
                    bpmnDisplaySwimlaneCombo.select(0);
                }
            }
        });
    }

    private void createBpmnDisplaySwimlaneCombo(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.bpmn.display.swimlane"));
        bpmnDisplaySwimlaneCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        bpmnDisplaySwimlaneCombo.setEnabled(false);
        for (SwimlaneDisplayMode mode : SwimlaneDisplayMode.values()) {
            bpmnDisplaySwimlaneCombo.add(mode.getLabel());
        }
        bpmnDisplaySwimlaneCombo.select(0);
        bpmnDisplaySwimlaneCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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

    private void verifyContentsValid() {
        if (!checkProjectValid()) {
            setErrorMessage(Localization.getString("error.choose_project"));
            setPageComplete(false);
        } else if (isProcessNameEmpty()) {
            setErrorMessage(Localization.getString("error.no_process_name"));
            setPageComplete(false);
        } else if (!isProcessNameValid()) {
            setErrorMessage(Localization.getString("error.process_name_not_valid"));
            setPageComplete(false);
        } else if (processExists()) {
            setErrorMessage(Localization.getString("error.process_already_exists"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    private boolean processExists() {
        return getProcessFolder().exists();
    }

    private boolean isProcessNameEmpty() {
        return processText.getText().length() == 0;
    }

    private boolean isProcessNameValid() {
        return ResourcesPlugin.getWorkspace().validateName(processText.getText(), IResource.FOLDER).isOK();
    }

    private boolean checkProjectValid() {
        if (projectCombo.getText().length() == 0) {
            return false;
        }
        return workspaceRoot.getFolder(getProcessFolderPath()).exists();
    }

    private IPath getProcessFolderPath() {
        return new Path(projectCombo.getText()).append("/src/process/");
    }

    private String getProcessName() {
        return processText.getText();
    }

    public Language getLanguage() {
        return Language.valueOf(languageCombo.getText());
    }

    public SwimlaneDisplayMode getSwimlaneDisplayMode() {
        return SwimlaneDisplayMode.values()[bpmnDisplaySwimlaneCombo.getSelectionIndex()];
    }

    public IFolder getProcessFolder() {
        IPath path = getProcessFolderPath().append(getProcessName());
        return workspaceRoot.getFolder(path);
    }
}

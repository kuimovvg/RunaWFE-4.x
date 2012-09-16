package ru.runa.bpm.ui.wizard;

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
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.pref.PrefConstants;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.ProjectFinder;

public class NewProcessDefinitionWizardPage extends WizardPage {

    private Combo projectCombo;
    private Text processText;
    private Combo versionCombo;
    private Combo notationCombo;
    private final IWorkspaceRoot workspaceRoot;
    private final IStructuredSelection selection;

    public NewProcessDefinitionWizardPage(IStructuredSelection selection) {
        super(Messages.getString("NewProcessDefinitionWizardPage.page.name"));
        this.selection = selection;
        this.workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        setTitle(Messages.getString("NewProcessDefinitionWizardPage.page.title"));
        setDescription(Messages.getString("NewProcessDefinitionWizardPage.page.description"));
    }

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
        createJpdlVersionCombo(composite);
        createNotationVersionCombo(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        setPageComplete(false);
        processText.setFocus();
    }

    private void createProjectField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("label.project"));

        projectCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (IProject project : ProjectFinder.getAllProjects()) {
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
        label.setText(Messages.getString("label.process_name"));
        processText = new Text(parent, SWT.BORDER);
        processText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        processText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createJpdlVersionCombo(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("label.jpdl_version"));
        versionCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (String jpdlVersion : JpdlVersionRegistry.getAllJpdlVersions()) {
            versionCombo.add(jpdlVersion);
        }
        versionCombo.select(1);
        versionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createNotationVersionCombo(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("label.notation"));
        notationCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        notationCombo.add("uml");
        notationCombo.add("bpmn");
        String defaultNotation = DesignerPlugin.getPrefString(PrefConstants.P_DEFAULT_NOTATION);
        notationCombo.setText(defaultNotation);
        notationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
            setErrorMessage(Messages.getString("error.choose_project"));
            setPageComplete(false);
        } else if (isProcessNameEmpty()) {
            setErrorMessage(Messages.getString("error.no_process_name"));
            setPageComplete(false);
        } else if (!isProcessNameValid()) {
            setErrorMessage(Messages.getString("error.process_name_not_valid"));
            setPageComplete(false);
        } else if (processExists()) {
            setErrorMessage(Messages.getString("error.process_already_exists"));
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

    public String getJpdlVersion() {
        return versionCombo.getText();
    }

    public String getNotation() {
        return notationCombo.getText();
    }

    public IFolder getProcessFolder() {
        IPath path = getProcessFolderPath().append(getProcessName());
        return workspaceRoot.getFolder(path);
    }

}

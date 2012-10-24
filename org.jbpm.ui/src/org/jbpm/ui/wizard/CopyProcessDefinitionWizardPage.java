package org.jbpm.ui.wizard;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
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
import org.jbpm.ui.JpdlVersionRegistry;
import org.jbpm.ui.ProcessCache;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.ProjectFinder;

public class CopyProcessDefinitionWizardPage extends WizardPage {
    private Combo projectCombo;
    private Text processText;
    private Combo versionCombo;
    private Combo notationCombo;
    private final IWorkspaceRoot workspaceRoot;
    private final IFolder sourceProcessFolder;
    private final ProcessDefinition sourceDefinition;

    public CopyProcessDefinitionWizardPage(IFolder sourceProcessFolder) {
        super(Messages.getString("CopyProcessDefinitionWizardPage.page.name"));
        this.sourceProcessFolder = sourceProcessFolder;
        this.workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        setTitle(Messages.getString("CopyProcessDefinitionWizardPage.page.title"));
        setDescription(Messages.getString("CopyProcessDefinitionWizardPage.page.description"));
        sourceDefinition = ProcessCache.getProcessDefinition(sourceProcessFolder.getName());
        if (sourceDefinition == null) {
            throw new NullPointerException("Process definition is null");
        }
    }

    public IFolder getSourceProcessFolder() {
        return sourceProcessFolder;
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
        
        projectCombo.setText(sourceProcessFolder.getParent().getParent().getParent().getName());
        
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
        processText.setText(sourceProcessFolder.getName());
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
        versionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        versionCombo.setEnabled(false);
        
        versionCombo.setText(sourceDefinition.getJpdlVersion());
    }

    private void createNotationVersionCombo(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("label.notation"));
        notationCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        notationCombo.add("uml");
        notationCombo.add("bpmn");
        notationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        notationCombo.setText(sourceDefinition.getNotation());
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
        return getTargetProcessFolder().exists();
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

    public String getProcessName() {
        return processText.getText();
    }

    public String getNotation() {
        return notationCombo.getText();
    }

    public IFolder getTargetProcessFolder() {
        IPath path = getProcessFolderPath().append(getProcessName());
        return workspaceRoot.getFolder(path);
    }

}

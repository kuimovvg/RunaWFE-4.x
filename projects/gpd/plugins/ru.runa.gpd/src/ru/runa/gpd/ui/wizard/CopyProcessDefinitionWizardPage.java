package ru.runa.gpd.ui.wizard;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.data.util.ProjectStructureUtils;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.dialog.projectstructure.ProjectStructureDirectory;
import ru.runa.gpd.ui.widget.treecombo.TreeCombo;
import ru.runa.gpd.ui.widget.treecombo.TreeComboItem;
import ru.runa.gpd.util.ProjectFinder;

public class CopyProcessDefinitionWizardPage extends WizardPage {
    private Combo projectCombo;
    private Text processText;
    private Combo languageCombo;
    private final IWorkspaceRoot workspaceRoot;
    private final IFolder sourceProcessFolder;
    private final ProcessDefinition sourceDefinition;
    private TreeCombo categoryCombo;
    private Button openProjectStructureDirectoryBtn;

    public CopyProcessDefinitionWizardPage(IFolder sourceProcessFolder) {
        super(Localization.getString("CopyProcessDefinitionWizardPage.page.name"));
        this.sourceProcessFolder = sourceProcessFolder;
        this.workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        setTitle(Localization.getString("CopyProcessDefinitionWizardPage.page.title"));
        setDescription(Localization.getString("CopyProcessDefinitionWizardPage.page.description"));
        sourceDefinition = ProcessCache.getFirstProcessDefinition(sourceProcessFolder.getName());
        if (sourceDefinition == null) {
            throw new NullPointerException("Process definition is null");
        }
    }

    public IFolder getSourceProcessFolder() {
        return sourceProcessFolder;
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
        createCategoryCombo(composite);
        createProcessNameField(composite);
        createJpdlVersionCombo(composite);        
        
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
        projectCombo.setText(sourceProcessFolder.getParent().getParent().getParent().getName());
        projectCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                reloadProjectStructureCombo();
                verifyContentsValid();                
            }
        });
    }

    private void createProcessNameField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.process_name"));
        processText = new Text(parent, SWT.BORDER);
        processText.setText(sourceProcessFolder.getName());
        processText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        processText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createJpdlVersionCombo(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.language"));
        languageCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        languageCombo.setEnabled(false);
        for (Language language : Language.values()) {
            languageCombo.add(language.name());
        }
        languageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        languageCombo.setText(sourceDefinition.getLanguage().name());
    }
    
    private void createCategoryCombo(Composite parent) {
    	Label label = new Label(parent, SWT.NONE);
    	label.setText(Localization.getString("label.process_category"));
    	
    	Composite directoryButtonsPanel = new Composite(parent, SWT.NONE);
    	GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        directoryButtonsPanel.setLayout(layout);
        directoryButtonsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));  
    	
    	int style = SWT.BORDER | SWT.READ_ONLY | SWT.BORDER;
    	categoryCombo = new TreeCombo(directoryButtonsPanel, style);
    	categoryCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	categoryCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                verifyContentsValid();
            }
    	});
    	
    	openProjectStructureDirectoryBtn = new Button(directoryButtonsPanel, SWT.NONE);
    	openProjectStructureDirectoryBtn.setToolTipText(Localization.getString("button.open_project_structure_directory"));    	
    	Image directoryBtnImage = SharedImages.getImageDescriptor("icons/filenav_nav.gif").createImage();
    	openProjectStructureDirectoryBtn.setImage(directoryBtnImage);
    	openProjectStructureDirectoryBtn.addListener(SWT.Selection, new Listener() {
    		
    		@Override
    		public void handleEvent(Event event) {
    			openProjectStructureDirectory();
    		}
    	});

    	reloadProjectStructureCombo();
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
        } else if (processCategoryIsEmpty()) {
            setErrorMessage(Localization.getString("error.category_is_empty"));
            setPageComplete(false); 
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }
    
    private boolean processCategoryIsEmpty() {
        return categoryCombo.getSelection().length == 0;
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

    public Language getLanguage() {
        return Language.valueOf(languageCombo.getText());
    }

    public IFolder getTargetProcessFolder() {
        IPath path = getProcessFolderPath().append(getProcessName());
        return workspaceRoot.getFolder(path);
    }
    
    private void reloadProjectStructureCombo() {
    	categoryCombo.select(null);
    	
    	String selectedProjectName = getProjectName();
		
		categoryCombo.removeAll();
		
		if (selectedProjectName != null) {    		    		    		    	        
        	ProjectStructureUtils.fillTreeCombo(categoryCombo, selectedProjectName, true);    		
    	}    	
    }
    
    public String[] getCategoryFullPath() {
    	TreeComboItem[] selection = categoryCombo.getSelection();
    	
    	if (selection.length > 0) {
    		return selection[0].getFullPath();
    	}
    	
    	return new String[0];
    }
    
    private void openProjectStructureDirectory() {
    	ProjectStructureDirectory dialog = new ProjectStructureDirectory(getProjectName());    	
    	dialog.open();
    	
    	reloadProjectStructureCombo();
    	switch(dialog.getReturnCode()) {
    		case Window.OK:    			
    			categoryCombo.selectByFullPath(dialog.getSelectedCategoryFullPath());
    			break;
    		case Window.CANCEL:
    			//Do nothing
    			break;
    	}
    }
    
    private String getProjectName() {
    	int selectedIndex = projectCombo.getSelectionIndex();
    	
    	if (selectedIndex != -1) {
    		return projectCombo.getItem(selectedIndex);    		
    	}
    	
    	return null;
    }
}

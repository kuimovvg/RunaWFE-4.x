package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.data.util.ProjectStructureUtils;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.dialog.projectstructure.ProjectStructureDirectory;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.ui.widget.treecombo.TreeCombo;
import ru.runa.gpd.ui.widget.treecombo.TreeComboItem;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ProjectFinder;
import ru.runa.gpd.wfe.WFEServerProcessDefinitionImporter;
import ru.runa.wfe.definition.dto.WfDefinition;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class ImportParWizardPage extends ImportWizardPage {
    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedParsLabel;
    private Button selectParsButton;
    private Button importFromServerButton;
    private TreeViewer serverDefinitionViewer;
    private String selectedDirFileName;
    private String[] selectedFileNames;
    private TreeCombo categoryCombo;
    private Button openProjectStructureDirectoryBtn;

    public ImportParWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(Localization.getString("ImportParWizardPage.page.title"));
        setDescription(Localization.getString("ImportParWizardPage.page.description"));
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite projectPanel = new Composite(sashForm, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 1;        
        projectPanel.setLayout(layout);
        projectPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        
        createProjectsGroup(projectPanel);
        
        Composite processCategoryPanel = new Composite(projectPanel, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        processCategoryPanel.setLayout(layout);
        processCategoryPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));       
        
        createCategoryCombo(processCategoryPanel);       
        
        Group importGroup = new Group(sashForm, SWT.NONE);
        importGroup.setLayout(new GridLayout(1, false));
        importGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        importFromFileButton = new Button(importGroup, SWT.RADIO);
        importFromFileButton.setText(Localization.getString("ImportParWizardPage.page.importFromFileButton"));
        importFromFileButton.setSelection(true);
        importFromFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setImportMode();
            }
        });
        fileSelectionArea = new Composite(importGroup, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        fileSelectionData.heightHint = 30;
        fileSelectionArea.setLayoutData(fileSelectionData);
        GridLayout fileSelectionLayout = new GridLayout();
        fileSelectionLayout.numColumns = 2;
        fileSelectionLayout.makeColumnsEqualWidth = false;
        fileSelectionLayout.marginWidth = 0;
        fileSelectionLayout.marginHeight = 0;
        fileSelectionArea.setLayout(fileSelectionLayout);
        selectedParsLabel = new Text(fileSelectionArea, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        selectedParsLabel.setLayoutData(gridData);
        selectParsButton = new Button(fileSelectionArea, SWT.PUSH);
        selectParsButton.setText(Localization.getString("button.choose"));
        selectParsButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        selectParsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                dialog.setFilterExtensions(new String[] { "*.par" });
                if (dialog.open() != null) {
                    selectedDirFileName = dialog.getFilterPath();
                    selectedFileNames = dialog.getFileNames();
                    String text = "";
                    for (String fileName : selectedFileNames) {
                        text += fileName + "\n";
                    }
                    selectedParsLabel.setText(text);
                }
            }
        });
        importFromServerButton = new Button(importGroup, SWT.RADIO);
        importFromServerButton.setText(Localization.getString("ImportParWizardPage.page.importFromServerButton"));
        SyncUIHelper.createHeader(importGroup, WFEServerProcessDefinitionImporter.getInstance(), WFEConnectionPreferencePage.class);
        createServerDefinitionsGroup(importGroup);
        setControl(pageControl);
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
        directoryButtonsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));  
    	
    	int style = SWT.BORDER | SWT.READ_ONLY | SWT.BORDER;
    	categoryCombo = new TreeCombo(directoryButtonsPanel, style);
    	categoryCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	
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

    private void setImportMode() {
        boolean fromFile = importFromFileButton.getSelection();
        selectParsButton.setEnabled(fromFile);
        if (fromFile) {
            serverDefinitionViewer.setInput(new Object());
        } else {
            if (WFEServerProcessDefinitionImporter.getInstance().isConfigured()) {
                if (!WFEServerProcessDefinitionImporter.getInstance().hasCachedData()) {
                    long start = System.currentTimeMillis();
                    WFEServerProcessDefinitionImporter.getInstance().synchronize();
                    long end = System.currentTimeMillis();
                    PluginLogger.logInfo("def sync [sec]: " + ((end - start) / 1000));
                }
                serverDefinitionViewer.setInput(WFEServerProcessDefinitionImporter.getInstance().loadCachedData());
                serverDefinitionViewer.refresh(true);
            }
        }
    }

    private void createServerDefinitionsGroup(Composite parent) {
        serverDefinitionViewer = new TreeViewer(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        serverDefinitionViewer.getControl().setLayoutData(gridData);
        serverDefinitionViewer.setContentProvider(new DefinitionTreeContentProvider());
        serverDefinitionViewer.setLabelProvider(new DefinitionLabelProvider());
        serverDefinitionViewer.setInput(new Object());
    }

    public boolean performFinish() {
        try {
            IProject project = getSelectedProject();
            String[] processNames;
            InputStream[] parInputStreams;
            boolean fromFile = importFromFileButton.getSelection();
            if (fromFile) {
                if (selectedDirFileName == null) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.selectValidPar"));
                }
                else if (processCategoryIsEmpty()) {
                	throw new Exception(Localization.getString("error.category_is_empty"));                    
                }
                processNames = new String[selectedFileNames.length];
                parInputStreams = new InputStream[selectedFileNames.length];
                for (int i = 0; i < selectedFileNames.length; i++) {
                    processNames[i] = selectedFileNames[i].substring(0, selectedFileNames[i].length() - 4);
                    String fileName = selectedDirFileName + File.separator + selectedFileNames[i];
                    parInputStreams[i] = new FileInputStream(fileName);
                }
            } else {
                List<?> selections = ((IStructuredSelection) serverDefinitionViewer.getSelection()).toList();
                List<WfDefinition> defSelections = Lists.newArrayList();
                for (Object object : selections) {
                    if (object instanceof WfDefinition) {
                        defSelections.add((WfDefinition) object);
                    }
                }
                if (defSelections.isEmpty()) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.selectValidDefinition"));
                }
                processNames = new String[defSelections.size()];
                parInputStreams = new InputStream[defSelections.size()];
                for (int i = 0; i < processNames.length; i++) {
                    WfDefinition stub = defSelections.get(i);
                    processNames[i] = stub.getName();
                    byte[] par = WFEServerProcessDefinitionImporter.getInstance().loadPar(stub);
                    parInputStreams[i] = new ByteArrayInputStream(par);
                }
            }
            for (int i = 0; i < processNames.length; i++) {
                String processName = processNames[i];
                InputStream parInputStream = parInputStreams[i];
                IFolder processFolder = project.getFolder("src/process/" + processName);
                if (processFolder.exists()) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.processWithSameNameExists"));
                }
                processFolder.create(true, true, null);
                IOUtils.extractArchiveToFolder(parInputStream, processFolder);
                IFile definitionFile = ProjectFinder.getProcessDefinitionFile(processFolder);
                ProcessDefinition definition = ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                if (definition != null && !Objects.equal(definition.getName(), processFolder.getName())) {
                    // if par name differs from definition name
                    String projectPath = "src/process/" + definition.getName();
                    IPath destination = project.getFolder(projectPath).getFullPath();
                    processFolder.move(destination, true, false, null);
                    processFolder = project.getFolder(projectPath);
                    IFile movedDefinitionFile = ProjectFinder.getProcessDefinitionFile(processFolder);
                    ProcessCache.newProcessDefinitionWasCreated(movedDefinitionFile);
                    ProcessCache.invalidateProcessDefinition(definitionFile);
                }
                ProjectStructureUtils.addProcess(processName, getCategoryFullPath());
            }
        } catch (Exception exception) {
            PluginLogger.logErrorWithoutDialog("import par", exception);
            setErrorMessage(Throwables.getRootCause(exception).getMessage());
            return false;
        }
        return true;
    }

    public static class DefinitionTreeContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof HistoryRoot) {
                HistoryRoot historyRoot = (HistoryRoot) parentElement;
                List<WfDefinition> history = WFEServerProcessDefinitionImporter.getInstance().loadCachedData().get(historyRoot.definition);
                List<WfDefinition> result = Lists.newArrayList(history);
                result.remove(0);
                return result.toArray();
            }
            if (WFEServerProcessDefinitionImporter.getInstance().loadCachedData().containsKey(parentElement)) {
                return new Object[] { new HistoryRoot((WfDefinition) parentElement) };
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof HistoryRoot) {
                return true;
            }
            List<WfDefinition> history = WFEServerProcessDefinitionImporter.getInstance().loadCachedData().get(element);
            return (history != null && history.size() > 1);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Map) {
                ArrayList<WfDefinition> arrayList = new ArrayList<WfDefinition>();
                arrayList.addAll(((Map<WfDefinition, List<WfDefinition>>) inputElement).keySet());
                return arrayList.toArray(new WfDefinition[arrayList.size()]);
            }
            return new Object[0];
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    public static class HistoryRoot {
        private final WfDefinition definition;

        public HistoryRoot(WfDefinition stub) {
            this.definition = stub;
        }
    }

    public static class DefinitionLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof WfDefinition) {
                WfDefinition definition = (WfDefinition) element;
                if (WFEServerProcessDefinitionImporter.getInstance().loadCachedData().containsKey(definition)) {
                    return definition.getName();
                }
                return String.valueOf(definition.getVersion());
            }
            if (element instanceof HistoryRoot) {
                return Localization.getString("ImportParWizardPage.page.oldDefinitionVersions");
            }
            return super.getText(element);
        }
    }   
    
    private boolean processCategoryIsEmpty() {
        return categoryCombo.getSelection().length == 0;
    }
    
    private void reloadProjectStructureCombo() {    	
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
    	IProject selectedProject;
		try {			
			selectedProject = getSelectedProject();
			
			if (selectedProject != null) {    		
	    		return selectedProject.getName();		
	    	}
		} catch (Exception ex) {
			PluginLogger.logErrorWithoutDialog("Error getting selected project", ex);            
		}
		
		return null;
    }
}

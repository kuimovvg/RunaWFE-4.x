package ru.runa.bpm.ui.wizard;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.ProcessCache;
import ru.runa.bpm.ui.pref.WFEConnectionPreferencePage;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.sync.SyncUIHelper;
import ru.runa.bpm.ui.sync.WFEServerProcessDefinitionImporter;
import ru.runa.bpm.ui.util.IOUtils;
import ru.runa.bpm.ui.util.ProjectFinder;

import ru.runa.wf.ProcessDefinitionStub;

public class ImportParWizardPage extends ImportWizardPage {

    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedParsLabel;
    private Button selectParsButton;
    private Button importFromServerButton;
    private TreeViewer serverDefinitionViewer;
    private String selectedDirFileName;
    private String[] selectedFileNames;

    public ImportParWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(Messages.getString("ImportParWizardPage.page.title"));
        setDescription(Messages.getString("ImportParWizardPage.page.description"));
    }

    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        createProjectsGroup(sashForm);

        Group importGroup = new Group(sashForm, SWT.NONE);
        importGroup.setLayout(new GridLayout(1, false));
        importGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        importFromFileButton = new Button(importGroup, SWT.RADIO);
        importFromFileButton.setText(Messages.getString("ImportParWizardPage.page.importFromFileButton"));
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
        selectParsButton.setText(Messages.getString("button.choose"));
        selectParsButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        selectParsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                // dialog.setFileName(startingDirectory.getPath());
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

        // editor = new FileFieldEditor("fileSelect", Messages.getString("ImportParWizardPage.page.parFile"), fileSelectionArea);
        // editor.setFileExtensions(new String[] { "*.par" });

        importFromServerButton = new Button(importGroup, SWT.RADIO);
        importFromServerButton.setText(Messages.getString("ImportParWizardPage.page.importFromServerButton"));
        SyncUIHelper.createHeader(importGroup, WFEServerProcessDefinitionImporter.getInstance(), WFEConnectionPreferencePage.class);

        createServerDefinitionsGroup(importGroup);

        setControl(pageControl);
    }

    private void setImportMode() {
        boolean fromFile = importFromFileButton.getSelection();
        // editor.setEnabled(fromFile, fileSelectionArea);
        selectParsButton.setEnabled(fromFile);
        if (fromFile) {
            serverDefinitionViewer.setInput(new Object());
        } else {
            if (WFEServerProcessDefinitionImporter.getInstance().isConfigured()) {
                if (!WFEServerProcessDefinitionImporter.getInstance().hasCachedData()) {
                    long start = System.currentTimeMillis();
                    WFEServerProcessDefinitionImporter.getInstance().synchronize();
                    long end = System.currentTimeMillis();
                    DesignerLogger.logInfo("def sync [sec]: " + ((end - start) / 1000));
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
                    throw new Exception(Messages.getString("ImportParWizardPage.error.selectValidPar"));
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
                List<ProcessDefinitionStub> defSelections = new ArrayList<ProcessDefinitionStub>();
                for (Object object : selections) {
                    if (object instanceof ProcessDefinitionStub) {
                        defSelections.add((ProcessDefinitionStub) object);
                    }
                }
                if (defSelections.isEmpty()) {
                    throw new Exception(Messages.getString("ImportParWizardPage.error.selectValidDefinition"));
                }
                processNames = new String[defSelections.size()];
                parInputStreams = new InputStream[defSelections.size()];
                for (int i = 0; i < processNames.length; i++) {
                    ProcessDefinitionStub stub = defSelections.get(i);
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
                    throw new Exception(Messages.getString("ImportParWizardPage.error.processWithSameNameExists"));
                }
                processFolder.create(true, true, null);
                IOUtils.extractArchiveToFolder(parInputStream, processFolder);
                IFile definitionFile = ProjectFinder.getProcessDefinitionFile(processFolder);
                ProcessCache.newProcessDefinitionWasCreated(definitionFile);
            }
        } catch (Exception exception) {
            DesignerLogger.logErrorWithoutDialog("import par", exception);
            setErrorMessage(exception.getMessage());
            return false;
        }
        return true;
    }

    public static class DefinitionTreeContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof HistoryRoot) {
                HistoryRoot historyRoot = (HistoryRoot) parentElement;
                ProcessDefinitionStub[] history = WFEServerProcessDefinitionImporter.getInstance().loadCachedData().get(historyRoot.stub);
                ProcessDefinitionStub[] result = new ProcessDefinitionStub[history.length - 1];
                System.arraycopy(history, 1, result, 0, result.length);
                return result;
            }
            if (WFEServerProcessDefinitionImporter.getInstance().loadCachedData().containsKey(parentElement)) {
                return new Object[] { new HistoryRoot((ProcessDefinitionStub) parentElement) };
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
            ProcessDefinitionStub[] history = WFEServerProcessDefinitionImporter.getInstance().loadCachedData().get(element);
            return (history != null && history.length > 1);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Map) {
                ArrayList<ProcessDefinitionStub> arrayList = new ArrayList<ProcessDefinitionStub>();
                arrayList.addAll(((Map<ProcessDefinitionStub, ProcessDefinitionStub[]>) inputElement).keySet());
                return arrayList.toArray(new ProcessDefinitionStub[arrayList.size()]);
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

        private final ProcessDefinitionStub stub;

        public HistoryRoot(ProcessDefinitionStub stub) {
            this.stub = stub;
        }

    }

    public static class DefinitionLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof ProcessDefinitionStub) {
                ProcessDefinitionStub stub = (ProcessDefinitionStub) element;
                if (WFEServerProcessDefinitionImporter.getInstance().loadCachedData().containsKey(stub)) {
                    return stub.getName();
                }
                return String.valueOf(stub.getVersion());
            }
            if (element instanceof HistoryRoot) {
                return Messages.getString("ImportParWizardPage.page.oldDefinitionVersions");
            }
            return super.getText(element);
        }
    }
}

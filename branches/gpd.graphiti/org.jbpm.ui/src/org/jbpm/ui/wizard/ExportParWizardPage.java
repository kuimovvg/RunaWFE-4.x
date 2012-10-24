package org.jbpm.ui.wizard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.internal.wizards.datatransfer.IFileExporter;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.ProcessCache;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.pref.WFEConnectionPreferencePage;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.sync.SyncUIHelper;
import org.jbpm.ui.sync.WFEServerProcessDefinitionImporter;
import org.jbpm.ui.util.IOUtils;
import org.jbpm.ui.util.MappingContentProvider;
import org.jbpm.ui.util.ProjectFinder;
import org.jbpm.ui.view.ValidationErrorsView;

public class ExportParWizardPage extends WizardArchiveFileResourceExportPage1 {

    private final Map<String, IFile> definitionNameFileMap;
    private ListViewer definitionListViewer;
    private Button exportToFileButton;
    private Button exportToServerButton;

    protected ExportParWizardPage(IStructuredSelection selection) {
        super(selection);
        setTitle(Messages.getString("ExportParWizardPage.page.title"));
        setDescription(Messages.getString("ExportParWizardPage.page.description"));

        this.definitionNameFileMap = new TreeMap<String, IFile>();
        for (IFile file : ProcessCache.getAllProcessDefinitionsMap().keySet()) {
            ProcessDefinition definition = ProcessCache.getProcessDefinition(file);
            if (definition != null) {
                definitionNameFileMap.put(getKey(file.getProject(), definition), file);
            }
        }
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group processListGroup = new Group(sashForm, SWT.NONE);
        processListGroup.setLayout(new GridLayout(1, false));
        processListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        processListGroup.setText(Messages.getString("label.process"));
        createViewer(processListGroup);

        Group exportGroup = new Group(sashForm, SWT.NONE);
        exportGroup.setLayout(new GridLayout(1, false));
        exportGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        exportToFileButton = new Button(exportGroup, SWT.RADIO);
        exportToFileButton.setText(Messages.getString("ExportParWizardPage.page.exportToFileButton"));
        exportToFileButton.setSelection(true);

        createDestinationGroup(exportGroup);

        exportToServerButton = new Button(exportGroup, SWT.RADIO);
        exportToServerButton.setText(Messages.getString("ExportParWizardPage.page.exportToServerButton"));

        SyncUIHelper.createHeader(exportGroup, WFEServerProcessDefinitionImporter.getInstance(), WFEConnectionPreferencePage.class);

        restoreWidgetValues();
        giveFocusToDestination();
        setControl(pageControl);
        setPageComplete(false);

        IFile adjacentFile = ProjectFinder.getCurrentFile();
        if (adjacentFile != null && adjacentFile.getParent().exists()) {
            IFile definitionFile = ProjectFinder.getProcessDefinitionFile((IFolder) adjacentFile.getParent());
            if (definitionFile != null && definitionFile.exists()) {
                ProcessDefinition currentDefinition = ProcessCache.getProcessDefinition(definitionFile);
                if (currentDefinition != null) {
                    definitionListViewer.setSelection(new StructuredSelection(getKey(definitionFile.getProject(), currentDefinition)));
                }
            }
        }
    }

    private void createViewer(Composite parent) {
        // process selection
        definitionListViewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        definitionListViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        definitionListViewer.setContentProvider(new ArrayContentProvider());
        definitionListViewer.setInput(definitionNameFileMap.keySet());
        definitionListViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(!event.getSelection().isEmpty());
            }

        });
    }

    private String getKey(IProject project, ProcessDefinition definition) {
        return project.getName() + "/" + definition.getName();
    }

    private String getProcessDefinitionSelection() {
        return (String) ((IStructuredSelection) definitionListViewer.getSelection()).getFirstElement();
    }

    @Override
    protected String getDestinationLabel() {
        return Messages.getString("ExportParWizardPage.label.destination_file");
    }

    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.par", "*.*" });
        String selectionName = getProcessDefinitionSelection();
        if (selectionName != null) {
            dialog.setFileName(selectionName.substring(selectionName.lastIndexOf("/") + 1) + ".par");
        }
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }
        String selectedFileName = dialog.open();

        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);
        }
    }

    @Override
    protected void updatePageCompletion() {
        setPageComplete(true);
    }

    @Override
    public boolean finish() {
        boolean exportToFile = exportToFileButton.getSelection();
        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();

        String selectedDefinitionName = getProcessDefinitionSelection();
        if (selectedDefinitionName == null) {
            setErrorMessage(Messages.getString("ExportParWizardPage.error.selectProcess"));
            return false;
        }
        IFile definitionFile = definitionNameFileMap.get(selectedDefinitionName);
        try {
            ProjectFinder.refreshProcessFolder(definitionFile);
        } catch (CoreException e1) {
        }
        ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
        try {
            int validationResult = definition.validateDefinition(definitionFile);
            if (!exportToFile && validationResult != 0) {
                DesignerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ValidationErrorsView.ID);
                if (validationResult == 2) {
                    setErrorMessage(Messages.getString("ExportParWizardPage.page.errorsExist"));
                    return false;
                }
            }
            definition.getContentProvider().validateProcessDefinitionXML(definitionFile);
            if (exportToFile && !ensureTargetIsValid()) {
                setErrorMessage(Messages.getString("ExportParWizardPage.error.selectDestinationPath"));
                return false;
            }
            if (!exportToFile && !WFEServerProcessDefinitionImporter.getInstance().isConfigured()) {
                SyncUIHelper.openConnectionSettingsDialog(WFEConnectionPreferencePage.class);
                if (!WFEServerProcessDefinitionImporter.getInstance().isConfigured()) {
                    return false;
                }
            }

            // Add orgfunctions & mappings .xml
            File orgFile = MappingContentProvider.INSTANCE.getOrgFile();
            if (orgFile.exists()) {
                InputStream is = new FileInputStream(orgFile);
                IFile file = IOUtils.getAdjacentFile(definitionFile, MappingContentProvider.ORG_XML_FILE_NAME);
                if (file.exists()) {
                    file.setContents(is, true, false, null);
                } else {
                    file.create(is, true, null);
                }
            }
            File mappingFile = MappingContentProvider.INSTANCE.getMappingFile();
            if (mappingFile.exists()) {
                InputStream is = new FileInputStream(mappingFile);
                IFile file = IOUtils.getAdjacentFile(definitionFile, MappingContentProvider.MAPPING_XML_FILE_NAME);
                if (file.exists()) {
                    file.setContents(is, true, false, null);
                } else {
                    file.create(is, true, null);
                }
            }

            List<IFile> resourcesToExport = new ArrayList<IFile>();
            IFolder processFolder = (IFolder) definitionFile.getParent();
            processFolder.refreshLocal(1, null);
            IResource[] members = processFolder.members();
            for (IResource resource : members) {
                if (resource instanceof IFile) {
                    resourcesToExport.add((IFile) resource);
                }
            }
            // TODO getContainer().run
            if (exportToFile) {
                new ParExportOperation(resourcesToExport, new FileOutputStream(getDestinationValue())).run(null);
            } else {
                new ParDeployOperation(resourcesToExport, definition.getName()).run(null);
            }
            return true;
        } catch (Exception e) {
            if (e.getMessage() != null) {
                setErrorMessage(e.getMessage());
            }
            DesignerLogger.logErrorWithoutDialog(Messages.getString("ExportParWizardPage.error.export"), e);
            return false;
        }
    }

    @Override
    protected String getOutputSuffix() {
        return ".par";
    }

    private final static String STORE_DESTINATION_NAMES_ID = "WizardParExportPage1.STORE_DESTINATION_NAMES_ID";

    @Override
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }

            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
        }
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null || directoryNames.length == 0) {
                return; // ie.- no settings stored
            }
            // destination
            setDestinationValue(directoryNames[0]);
            for (int i = 0; i < directoryNames.length; i++) {
                addDestinationItem(directoryNames[i]);
            }

        }
    }

    private static class ParExportOperation implements IRunnableWithProgress {

        protected final OutputStream outputStream;
        protected final List<IFile> resourcesToExport;

        public ParExportOperation(List<IFile> resourcesToExport, OutputStream outputStream) {
            this.outputStream = outputStream;
            this.resourcesToExport = resourcesToExport;
        }

        protected void exportResource(IFileExporter exporter, IFile fileResource, IProgressMonitor progressMonitor) throws IOException, CoreException {
            if (!fileResource.isSynchronized(1)) {
                fileResource.refreshLocal(1, null);
            }
            if (!fileResource.isAccessible()) {
                return;
            }
            String destinationName = fileResource.getName();
            //progressMonitor.subTask(destinationName);
            exporter.write(fileResource, destinationName);
            //progressMonitor.worked(1);
        }

        protected void exportResources(IProgressMonitor progressMonitor) throws InvocationTargetException {
            try {
                ParFileExporter exporter = new ParFileExporter(outputStream);
                //progressMonitor.beginTask("", totalWork);
                for (IFile resource : resourcesToExport) {
                    exportResource(exporter, resource, progressMonitor);
                    //ModalContext.checkCanceled(progressMonitor);
                }
                exporter.finished();
                outputStream.flush();
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }

        public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
//            try {
                exportResources(progressMonitor);
//            } finally {
//                progressMonitor.done();
//            }
        }

    }

    private class ParDeployOperation extends ParExportOperation {

        private final String definitionName;

        public ParDeployOperation(List<IFile> resourcesToExport, String definitionName) {
            super(resourcesToExport, new ByteArrayOutputStream());
            this.definitionName = definitionName;
        }

        @Override
        public void run(final IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
            try {
                exportResources(progressMonitor);
                final ByteArrayOutputStream baos = (ByteArrayOutputStream) outputStream;
                try {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                WFEServerProcessDefinitionImporter.getInstance().uploadPar(definitionName, baos.toByteArray());
                            } catch (Exception e) {
                                //progressMonitor.setCanceled(true);
                                DesignerLogger.logErrorWithoutDialog(Messages.getString("ExportParWizardPage.error.export"), e);
                                ExportParWizardPage.this.setErrorMessage(e.getMessage());
                            }
                        }

                    });
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                }
            } finally {
//                if (progressMonitor.isCanceled()) {
//                    throw new InterruptedException();
//                } else {
//                    progressMonitor.done();
//                }
            }
        }
    }

    private static class ParFileExporter implements IFileExporter {

        private final JarOutputStream jarOutputStream;

        public ParFileExporter(OutputStream outputStream) throws IOException {
            this.jarOutputStream = new JarOutputStream(outputStream, new Manifest());
        }

        @Override
        public void finished() throws IOException {
            jarOutputStream.close();
        }

        private void write(JarEntry entry, IFile contents) throws IOException, CoreException {
            byte[] readBuffer = new byte[1024];
            jarOutputStream.putNextEntry(entry);
            InputStream contentStream = contents.getContents();
            try {
                int n;
                while ((n = contentStream.read(readBuffer)) > 0) {
                    jarOutputStream.write(readBuffer, 0, n);
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            jarOutputStream.closeEntry();
        }

        @Override
        public void write(IFile resource, String destinationPath) throws IOException, CoreException {
            JarEntry newEntry = new JarEntry(destinationPath);
            write(newEntry, resource);
        }

		@Override
		public void write(IContainer container, String destinationPath)
				throws IOException {
			throw new UnsupportedOperationException();
		}
        
        
    }
}

package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.internal.wizards.datatransfer.IFileExporter;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;

import ru.runa.gpd.GPDProject;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.ProjectFinder;

import com.google.common.base.Throwables;

public class ExportProjectWizardPage extends WizardArchiveFileResourceExportPage1 {

    private Button exportToFileButton;

    private IProject selectedProject;

    protected ExportProjectWizardPage(IStructuredSelection selection) {
        super(selection);
        setTitle(Localization.getString("ExportProjectWizardPage.page.title"));

        this.selectedProject = (IProject) selection.getFirstElement();

        String descriptionMessage = Localization.getString("ExportProjectWizardPage.page.description");
        setDescription(MessageFormat.format(descriptionMessage, selectedProject.getName()));
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group exportGroup = new Group(sashForm, SWT.NONE);
        exportGroup.setLayout(new GridLayout(1, false));
        exportGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        createDestinationGroup(exportGroup);

        restoreWidgetValues();
        giveFocusToDestination();
        setControl(pageControl);
        setPageComplete(getDestinationValue() != null);
    }

    @Override
    protected String getDestinationLabel() {
        return Localization.getString("ExportProjectWizardPage.label.destination_file");
    }

    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.wba", "*.*" });
        String selectionName = selectedProject.getName();
        if (selectionName != null) {
            dialog.setFileName(selectionName.substring(selectionName.lastIndexOf("/") + 1) + ".wba");
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
        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();

        String selectedProjectName = selectedProject.getName();

        if (selectedProjectName == null) {
            setErrorMessage(Localization.getString("ExportProjectWizardPage.error.selectProject"));
            return false;
        }

        Map<String, IFile> projectProcessesFiles = new HashMap<String, IFile>();
        for (IFile file : ProcessCache.getAllProcessDefinitionsMap().keySet()) {
            ProcessDefinition definition = ProcessCache.getProcessDefinition(file);
            if (definition != null && selectedProjectName.equals(file.getProject().getName())) {
                projectProcessesFiles.put(definition.getName(), file);
            }
        }

        Map<String, ProcessDefinition> projectProcesses = new HashMap<String, ProcessDefinition>();

        for (IFile definitionFile : projectProcessesFiles.values()) {
            try {
                ProjectFinder.refreshProcessFolder(definitionFile);
            } catch (CoreException e1) {
            }
            ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
            projectProcesses.put(definition.getName(), definition);
        }

        Map<String, List<IFile>> processesResourcesToExport = new HashMap<String, List<IFile>>();

        try {
            for (String processName : projectProcessesFiles.keySet()) {
                IFile definitionFile = projectProcessesFiles.get(processName);
                ProcessDefinition definition = projectProcesses.get(processName);

                int validationResult = definition.validateDefinition(definitionFile);
                definition.getLanguage().getSerializer().validateProcessDefinitionXML(definitionFile);
                List<IFile> resourcesToExport = new ArrayList<IFile>();
                IFolder processFolder = (IFolder) definitionFile.getParent();
                processFolder.refreshLocal(1, null);
                IResource[] members = processFolder.members();
                for (IResource resource : members) {
                    if (resource instanceof IFile) {
                        resourcesToExport.add((IFile) resource);
                    }
                }

                String msg = MessageFormat.format(Localization.getString("ExportProjectWizardPage.confirm.export.invalid.process"), definition.getName());
                if (definition.isInvalid() && !MessageDialog.openConfirm(getShell(), Localization.getString("message.confirm.operation"), msg)) {
                    return false;
                }

                processesResourcesToExport.put(definition.getName(), resourcesToExport);
            }

            if (!selectedProject.isOpen()) {
                selectedProject.open(null);
            }

            List<IFile> rootFiles = new ArrayList<IFile>(2);
            rootFiles.add(selectedProject.getFile(GPDProject.STRUCTURE_DESCRIPTOR_FILENAME));
            rootFiles.add(selectedProject.getFile(GPDProject.DATASOURCE_FILE_NAME));
            rootFiles.add(selectedProject.getFile(GPDProject.FUNCTIONS_DESCRIPTOR_FILENAME));
            processesResourcesToExport.put(null, rootFiles);

            if (!ensureTargetIsValid()) {
                setErrorMessage(Localization.getString("ExportProjectWizardPage.error.selectDestinationPath"));
                return false;
            }

            new ProjectExportOperation(processesResourcesToExport, new FileOutputStream(getDestinationValue())).run(null);
            return true;
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog(Localization.getString("ExportProjectWizardPage.error.export"), th);
            setErrorMessage(Throwables.getRootCause(th).getMessage());
            return false;
        }
    }

    @Override
    protected String getOutputSuffix() {
        return ".wba";
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

    private static class ProjectExportOperation implements IRunnableWithProgress {
        protected final OutputStream outputStream;
        protected final Map<String, List<IFile>> processesResourcesToExport;

        public ProjectExportOperation(Map<String, List<IFile>> processesResourcesToExport, OutputStream outputStream) {
            this.outputStream = outputStream;
            this.processesResourcesToExport = processesResourcesToExport;
        }

        protected void exportResource(IFileExporter exporter, IFile fileResource, IProgressMonitor progressMonitor) throws IOException, CoreException {
            if (!fileResource.isSynchronized(1)) {
                fileResource.refreshLocal(1, null);
            }
            if (!fileResource.isAccessible()) {
                return;
            }
            String destinationName = fileResource.getName();
            exporter.write(fileResource, destinationName);
        }

        protected void exportResource(ProjectFileExporter exporter, String destinationName, byte[] content, IProgressMonitor progressMonitor) throws IOException, CoreException {
            exporter.write(new ByteArrayInputStream(content), destinationName);
        }

        protected void exportResources(IProgressMonitor progressMonitor) throws InvocationTargetException {
            try {
                Map<String, ByteArrayOutputStream> processesStreams = new HashMap<String, ByteArrayOutputStream>();
                for (String processName : processesResourcesToExport.keySet()) {
                    if (processName != null) {
                        ByteArrayOutputStream processStream = new ByteArrayOutputStream();
                        ParFileExporter exporter = new ParFileExporter(processStream);
                        for (IFile resource : processesResourcesToExport.get(processName)) {
                            exportResource(exporter, resource, progressMonitor);
                        }
                        exporter.finished();
                        processStream.flush();

                        processesStreams.put(processName, processStream);
                    }
                }

                ProjectFileExporter projectExporter = new ProjectFileExporter(outputStream);
                for (Entry<String, ByteArrayOutputStream> processStreamEntry : processesStreams.entrySet()) {
                    exportResource(projectExporter, processStreamEntry.getKey().concat(".par"), processStreamEntry.getValue().toByteArray(), progressMonitor);
                }

                List<IFile> commonFiles = processesResourcesToExport.get(null);
                for (IFile resource : commonFiles) {
                    exportResource(projectExporter, resource, progressMonitor);
                }
                projectExporter.finished();
                outputStream.flush();

            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }

        @Override
        public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
            exportResources(progressMonitor);
        }
    }

    private static class ParFileExporter implements IFileExporter {
        private final ZipOutputStream outputStream;

        public ParFileExporter(OutputStream outputStream) throws IOException {
            this.outputStream = new ZipOutputStream(outputStream);
        }

        @Override
        public void finished() throws IOException {
            outputStream.close();
        }

        private void write(ZipEntry entry, IFile contents) throws IOException, CoreException {
            byte[] readBuffer = new byte[1024];
            outputStream.putNextEntry(entry);
            InputStream contentStream = contents.getContents();
            try {
                int n;
                while ((n = contentStream.read(readBuffer)) > 0) {
                    outputStream.write(readBuffer, 0, n);
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            outputStream.closeEntry();
        }

        @Override
        public void write(IFile resource, String destinationPath) throws IOException, CoreException {
            ZipEntry newEntry = new ZipEntry(destinationPath);
            write(newEntry, resource);
        }

        @Override
        public void write(IContainer container, String destinationPath) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    private static class ProjectFileExporter implements IFileExporter {
        private final ZipOutputStream outputStream;

        public ProjectFileExporter(OutputStream outputStream) throws IOException {
            this.outputStream = new ZipOutputStream(outputStream);
        }

        @Override
        public void finished() throws IOException {
            outputStream.close();
        }

        private void write(ZipEntry entry, IFile contents) throws IOException, CoreException {
            byte[] readBuffer = new byte[1024];
            outputStream.putNextEntry(entry);
            InputStream contentStream = contents.getContents();
            try {
                int n;
                while ((n = contentStream.read(readBuffer)) > 0) {
                    outputStream.write(readBuffer, 0, n);
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            outputStream.closeEntry();
        }

        private void write(ZipEntry entry, InputStream contentStream) throws IOException, CoreException {
            byte[] readBuffer = new byte[1024];
            outputStream.putNextEntry(entry);
            try {
                int n;
                while ((n = contentStream.read(readBuffer)) > 0) {
                    outputStream.write(readBuffer, 0, n);
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            outputStream.closeEntry();
        }

        @Override
        public void write(IFile resource, String destinationPath) throws IOException, CoreException {
            ZipEntry newEntry = new ZipEntry(destinationPath);
            write(newEntry, resource);
        }

        public void write(InputStream contentStream, String destinationPath) throws IOException, CoreException {
            ZipEntry newEntry = new ZipEntry(destinationPath);
            write(newEntry, contentStream);
        }

        @Override
        public void write(IContainer container, String destinationPath) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

}

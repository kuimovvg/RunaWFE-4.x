package ru.runa.gpd.ui.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import ru.runa.gpd.Activator;
import ru.runa.gpd.GPDProject;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.data.util.ProjectStructureUtils;
import ru.runa.gpd.exception.InvalidDbConfigurationFileException;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ProjectComponentsCreator;
import ru.runa.gpd.util.ProjectFinder;
import ru.runa.gpd.util.WorkspaceOperations;

public class ImportProjectWizard extends Wizard implements IExportWizard {
    private ImportProjectWizardPage page;
    private IProject importedProject;

    public ImportProjectWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("ImportProjectWizard");
        if (section == null) {
            section = workbenchSettings.addNewSection("ImportProjectWizard");
        }
        setDialogSettings(section);
        setWindowTitle(Localization.getString("ImportProjectWizard.wizard.title"));
    }

    @Override
    public void addPages() {
        page = new ImportProjectWizardPage(null);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Localization.getString("ImportProjectWizard.monitor.title"), 9);
                        monitor.worked(1);
                        importedProject = makeProject();
                        monitor.worked(1);
                        IJavaProject javaProject = JavaCore.create(importedProject);
                        monitor.worked(1);
                        ProjectComponentsCreator.createOutputLocation(javaProject);
                        monitor.worked(1);
                        ProjectComponentsCreator.addJavaBuilder(javaProject);
                        monitor.worked(1);
                        ProjectComponentsCreator.setClasspath(javaProject);
                        monitor.worked(1);
                        ProjectComponentsCreator.createProjectProperties(javaProject, page.getCorrectFileName());
                        monitor.worked(1);
                        importedProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
                        monitor.worked(1);
                        createProcesses();
                        monitor.worked(1);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            if (!(e.getCause() instanceof InvalidDbConfigurationFileException)) {
                PluginLogger.logError(Localization.getString("ImportProjectWizard.error.creation"), e.getTargetException());
            } else {
                PluginLogger.logError(Localization.getString("dbConfigurationFileParseError"), e.getTargetException());
            }
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    private IProject makeProject() throws Exception {
        final IProject importedProject = new GPDProject(ResourcesPlugin.getWorkspace().getRoot().getProject(page.getCorrectFileName()));
        final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(page.getCorrectFileName());

        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 3000);
                    importedProject.create(description, new SubProgressMonitor(monitor, 1000));
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    importedProject.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));
                    importedProject.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1000));
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw (Exception) e.getTargetException();
        }
        return importedProject;
    }

    public void createProcesses() {
        try {
            new ProjectImportOperation(page.getSelectedDirFileName(), page.getSelectedFileNames(), importedProject, page.getCorrectFileName()).run(null);
        } catch (Exception exception) {
            PluginLogger.logErrorWithoutDialog("import wba", exception);
        }
    }

    private static class ProjectImportOperation implements IRunnableWithProgress {
        String selectedDirFileName = null;
        String[] selectedFileNames = null;
        IProject importedProject = null;
        String correctFileName = null;

        public ProjectImportOperation(String selectedDirFileName, String[] selectedFileNames, IProject importedProject, String correctFileName) {
            super();
            this.selectedDirFileName = selectedDirFileName;
            this.selectedFileNames = selectedFileNames;
            this.importedProject = importedProject;
            this.correctFileName = correctFileName;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
                importResources(monitor);
            } catch (Exception ex) {
                throw new InvocationTargetException(ex);
            }
        }

        private void importResources(IProgressMonitor monitor) throws IOException, CoreException, Exception {
            ZipFile zip = new ZipFile(this.selectedDirFileName + File.separator + this.selectedFileNames[0]);
            Enumeration entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.getName() != null) {
                    InputStream in = zip.getInputStream(entry);
                    if (entry.getName().endsWith(".par")) {
                        String parFileName = entry.getName().replace(".par", "");
                        IFolder processFolder = importedProject.getFolder("src/process/" + parFileName);
                        if (processFolder.exists()) {
                            throw new Exception(Localization.getString("ImportParWizardPage.error.processWithSameNameExists"));
                        }
                        processFolder.create(true, true, null);
                        IOUtils.extractArchiveToFolder(in, processFolder);
                        IFile definitionFile = ProjectFinder.getProcessDefinitionFile(processFolder);
                        ProcessCache.newProcessDefinitionWasCreated(definitionFile);

                        ProjectStructureUtils.addProcess(parFileName, new String[] { correctFileName });
                    } else if (entry.getName().equals(GPDProject.DATASOURCE_FILE_NAME)) {
                        WorkspaceOperations.validateDbConfigFile(in);
                        in = zip.getInputStream(entry);
                        importXmlFile(GPDProject.DATASOURCE_FILE_NAME, in);
                        WorkspaceOperations.deleteDBResources(importedProject);
                    } else if (entry.getName().equals(GPDProject.FUNCTIONS_DESCRIPTOR_FILENAME)) {
                        importXmlFile(GPDProject.FUNCTIONS_DESCRIPTOR_FILENAME, in);
                    } else if (entry.getName().equals(GPDProject.STRUCTURE_DESCRIPTOR_FILENAME)) {
                        importXmlFile(GPDProject.STRUCTURE_DESCRIPTOR_FILENAME, in);
                    }
                }
            }
        }

        private void importXmlFile(String fileName, InputStream in) throws CoreException {
            IFile file = importedProject.getFile(fileName);
            file.refreshLocal(IResource.DEPTH_ONE, null);
            if (file.exists()) {
                file.delete(true, null);
            }
            file.refreshLocal(IResource.DEPTH_ONE, null);
            file.create(in, IResource.NONE, null);
            file.refreshLocal(IResource.DEPTH_ONE, null);
        }
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }
}

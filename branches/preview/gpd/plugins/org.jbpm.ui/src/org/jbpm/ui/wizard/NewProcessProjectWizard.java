package ru.runa.bpm.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.custom.CustomizationRegistry;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.IOUtils;
import ru.runa.bpm.ui.util.JbpmClasspathContainer;

public class NewProcessProjectWizard extends Wizard implements INewWizard {

    private final Path JBPM_CONTAINER_PATH = new Path("JBPM");
    private static final String SOURCE_LOCATION = "src/process";
    private static final String OUTPUT_LOCATION = "bin";

    private WizardNewProjectCreationPage mainPage;

    private IProject newProject;

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.getString("NewProcessProjectWizard.wizard.title"));
    }

    @Override
    public void addPages() {
        super.addPages();
        mainPage = new WizardNewProjectCreationPage("basicNewProjectPage");
        mainPage.setTitle(Messages.getString("NewProcessProjectWizard.page.title"));
        mainPage.setDescription(Messages.getString("NewProcessProjectWizard.page.description"));
        this.addPage(mainPage);
    }

    private IProject createNewProject() throws Exception {
        final IProject newProject = mainPage.getProjectHandle();
        final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(newProject.getName());
        if (!mainPage.useDefaults()) {
            description.setLocation(mainPage.getLocationPath());
        }
        description.setNatureIds(new String[]{ JavaCore.NATURE_ID });

        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 3000);
                    newProject.create(description, new SubProgressMonitor(monitor, 1000));
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    newProject.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));
                    newProject.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1000));
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
        return newProject;
    }

    private void createOutputLocation(IJavaProject javaProject) throws CoreException {
        IFolder binFolder = javaProject.getProject().getFolder(OUTPUT_LOCATION);
        IOUtils.createFolder(binFolder);
        javaProject.setOutputLocation(binFolder.getFullPath(), null);
    }

    private void addJavaBuilder(IJavaProject javaProject) throws CoreException {
        IProjectDescription desc = javaProject.getProject().getDescription();
        ICommand command = desc.newCommand();
        command.setBuilderName(JavaCore.BUILDER_ID);
        desc.setBuildSpec(new ICommand[] { command });
        javaProject.getProject().setDescription(desc, null);
    }

    private void setClasspath(IJavaProject javaProject) throws CoreException {
        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        //addSourceFolders
        IFolder folder = javaProject.getProject().getFolder(SOURCE_LOCATION);
        IOUtils.createFolder(folder);
        IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(folder);
        entries.add(JavaCore.newSourceEntry(root.getPath()));

        //addJRELibraries
        entries.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
        
        //addJbpmLibraries
        JavaCore.setClasspathContainer(JBPM_CONTAINER_PATH, new IJavaProject[] { javaProject },
                new IClasspathContainer[] { new JbpmClasspathContainer(javaProject, JBPM_CONTAINER_PATH) }, null);
        entries.add(JavaCore.newContainerEntry(JBPM_CONTAINER_PATH));
        
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Messages.getString("NewProcessProjectWizard.monitor.title"), 7);
                        newProject = createNewProject();
                        monitor.worked(1);
                        IJavaProject javaProject = JavaCore.create(newProject);
                        monitor.worked(1);
                        createOutputLocation(javaProject);
                        monitor.worked(1);
                        addJavaBuilder(javaProject);
                        monitor.worked(1);
                        setClasspath(javaProject);
                        monitor.worked(1);
                        newProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
                        monitor.worked(1);
                        CustomizationRegistry.init(javaProject);
                        monitor.worked(1);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            DesignerLogger.logError(Messages.getString("NewProcessProjectWizard.error.creation"), e.getTargetException());
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

}
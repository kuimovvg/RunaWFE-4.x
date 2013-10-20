package ru.runa.gpd.ui.wizard;

import java.lang.reflect.InvocationTargetException;

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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import ru.runa.gpd.GPDProject;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.exception.InvalidDbConfigurationFileException;
import ru.runa.gpd.util.ProjectComponentsCreator;
import ru.runa.gpd.util.WorkspaceOperations;

public class NewProcessProjectWizard extends Wizard implements INewWizard {
    private NewProcessProjectWizardPage mainPage;
    private IProject newProject;

    @Override
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        setWindowTitle(Localization.getString("NewProcessProjectWizard.wizard.title"));
    }

    @Override
    public void addPages() {
        super.addPages();
        mainPage = new NewProcessProjectWizardPage("basicNewProjectPage");
        mainPage.setTitle(Localization.getString("NewProcessProjectWizard.page.title"));
        mainPage.setDescription(Localization.getString("NewProcessProjectWizard.page.description"));
        this.addPage(mainPage);
    }

    private IProject createNewProject() throws Exception {
        final IProject newProject = new GPDProject(mainPage.getProjectHandle());
        final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(newProject.getName());
        if (!mainPage.useDefaults()) {
            description.setLocation(mainPage.getLocationPath());
        }
        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
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

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Localization.getString("NewProcessProjectWizard.monitor.title"), 7);
                        WorkspaceOperations.validateDbConfigFile(mainPage.getDBConfigFilePath());
                        monitor.worked(1);
                        newProject = createNewProject();
                        monitor.worked(1);
                        WorkspaceOperations.loadDbConfigFileIfPossible(newProject, mainPage.getDBConfigFilePath(), false);
                        monitor.worked(1);
                        IJavaProject javaProject = JavaCore.create(newProject);
                        monitor.worked(1);
                        ProjectComponentsCreator.createOutputLocation(javaProject);
                        monitor.worked(1);
                        ProjectComponentsCreator.addJavaBuilder(javaProject);
                        monitor.worked(1);
                        ProjectComponentsCreator.setClasspath(javaProject);
                        monitor.worked(1);
                        ProjectComponentsCreator.createProjectProperties(javaProject, newProject.getName());
                        monitor.worked(1);
                        newProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
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
                PluginLogger.logError(Localization.getString("NewProcessProjectWizard.error.creation"), e.getTargetException());
            }
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }
}

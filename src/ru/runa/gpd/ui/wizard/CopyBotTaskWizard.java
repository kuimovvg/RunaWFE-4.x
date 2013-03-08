package ru.runa.gpd.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.WorkspaceOperations;

public class CopyBotTaskWizard extends Wizard implements INewWizard {
    private IStructuredSelection selection;
    private NewBotTaskWizardPage page;

    public CopyBotTaskWizard() {
        setWindowTitle(Localization.getString("CopyProcessDefinitionWizard.wizard.title"));
    }

    @Override
    public void init(IWorkbench w, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        IFile sourceBotTask = (IFile) selection.getFirstElement();
        page = new NewBotTaskWizardPage(selection, sourceBotTask.getName());
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Localization.getString("CopyProcessDefinitionWizard.monitor.title"), 3);
                        monitor.worked(1);
                        IFolder botFolder = page.getBotFolder();
                        IFile sourceBotTask = (IFile) selection.getFirstElement();
                        sourceBotTask.copy(botFolder.getFullPath().append(page.getBotTaskName()), true, monitor);
                        monitor.worked(1);
                        //rename
                        IFile confFile = ResourcesPlugin.getWorkspace().getRoot()
                                .getFile(sourceBotTask.getParent().getFullPath().append(new Path(sourceBotTask.getName() + ".conf")));
                        if (confFile.exists()) {
                            BotTask botTaskCopy = BotCache.getBotTaskNotNull(sourceBotTask);
                            botTaskCopy.setName(page.getBotTaskName());
                            IFile botTaskFileCopy = ResourcesPlugin.getWorkspace().getRoot().getFile(botFolder.getFullPath().append(page.getBotTaskName()));
                            WorkspaceOperations.saveBotTask(botTaskFileCopy, botTaskCopy);
                            BotCache.newBotTaskHasBeenCreated(botFolder.getName(), botTaskFileCopy, botTaskCopy);
                        }
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (Exception e) {
            PluginLogger.logError(e);
            return false;
        }
        return true;
    }
}

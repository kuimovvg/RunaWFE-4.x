package ru.runa.gpd.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.BotTaskEditor;
import ru.runa.gpd.util.IOUtils;

public class NewBotTaskWizard extends Wizard implements INewWizard {
    private NewBotTaskWizardPage page;
    private boolean parameterized;
    private IStructuredSelection selection;
    private IWorkbench workbench;

    public NewBotTaskWizard(boolean parameterized) {
        this.parameterized = parameterized;
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new NewBotTaskWizardPage(selection);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("processing", 4);
                        IFolder folder = page.getBotFolder();
                        IFile botTaskFile = folder.getFile(page.getBotTaskName());
                        botTaskFile = IOUtils.createFileSafely(botTaskFile);
                        monitor.worked(1);
                        BotTaskEditor editor = (BotTaskEditor) IDE.openEditor(getActivePage(), botTaskFile, BotTaskEditor.ID, true);
                        editor.initBotTaskTypeExtended(parameterized);
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            PluginLogger.logError("bottask.error.creation", e.getTargetException());
            return false;
        } catch (InterruptedException e) {
        }
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        this.workbench = workbench;
        setNeedsProgressMonitor(true);
    }

    private IWorkbenchPage getActivePage() {
        return workbench.getActiveWorkbenchWindow().getActivePage();
    }
}

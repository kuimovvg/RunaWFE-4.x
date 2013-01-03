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

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.BotTaskEditor;
import ru.runa.gpd.util.BotTaskContentUtil;

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
                        // TODO move to par provider
                        monitor.beginTask(Localization.getString("NewProcessDefinitionWizard.monitor.title"), 4);
                        IFolder folder = page.getBotFolder();
                        IFile botTaskFile = folder.getFile(page.getBotTaskName());
                        botTaskFile.create(BotTaskContentUtil.createBotTaskInfo(), true, null);
                        monitor.worked(1);
                        BotTaskEditor editor = (BotTaskEditor) IDE.openEditor(getActivePage(), botTaskFile, BotTaskEditor.ID, true);
                        editor.setParameterized(parameterized);
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            PluginLogger.logError(Localization.getString("NewProcessDefinitionWizard.error.creation"), e.getTargetException());
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

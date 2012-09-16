package ru.runa.bpm.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.ProcessCache;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.ProjectFinder;
import ru.runa.bpm.ui.util.WorkspaceOperations;

public class CopyProcessDefinitionWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;

    private CopyProcessDefinitionWizardPage page;

    public CopyProcessDefinitionWizard() {
        setWindowTitle(Messages.getString("CopyProcessDefinitionWizard.wizard.title"));
    }

    public void init(IWorkbench w, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        IFolder sourceProcessFolder = (IFolder) selection.getFirstElement();
        page = new CopyProcessDefinitionWizardPage(sourceProcessFolder);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Messages.getString("CopyProcessDefinitionWizard.monitor.title"), 3);
                        monitor.worked(1);
                        IFolder targetFolder = page.getTargetProcessFolder();
                        page.getSourceProcessFolder().copy(targetFolder.getFullPath(), true, monitor);
                        IFile definitionFile = ProjectFinder.getProcessDefinitionFile(targetFolder);
                        monitor.worked(1);
                        ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
                        definition.setName(page.getProcessName());
                        definition.setNotation(page.getNotation());
                        WorkspaceOperations.saveProcessDefinition(definitionFile, definition);
                        ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (Exception e) {
            DesignerLogger.logError(e);
            return false;
        }
        return true;
    }

}

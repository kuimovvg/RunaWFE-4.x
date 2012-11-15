package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.dom4j.Document;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.par.GpdXmlContentProvider;
import ru.runa.gpd.util.ProjectFinder;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.gpd.util.XmlUtil;

public class NewProcessDefinitionWizard extends Wizard implements INewWizard {
    private IStructuredSelection selection;
    private IWorkbench workbench;
    private NewProcessDefinitionWizardPage page;

    public NewProcessDefinitionWizard() {
        setWindowTitle(Localization.getString("NewProcessDefinitionWizard.wizard.title"));
    }

    @Override
    public void init(IWorkbench w, IStructuredSelection currentSelection) {
        this.workbench = w;
        this.selection = currentSelection;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        page = new NewProcessDefinitionWizardPage(selection);
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
                        IFolder folder = page.getProcessFolder();
                        folder.create(true, true, null);
                        monitor.worked(1);
                        IFile definitionFile = ProjectFinder.getProcessDefinitionFile(folder);
                        String processName = page.getProcessFolder().getName();
                        Document document = page.getLanguage().getSerializer().getInitialProcessDefinitionDocument(processName);
                        byte[] bytes = XmlUtil.writeXml(document);
                        definitionFile.create(new ByteArrayInputStream(bytes), true, null);
                        monitor.worked(1);
                        IFile gpdFile = folder.getFile(GpdXmlContentProvider.GPD_FILE_NAME);
                        gpdFile.create(createInitialGpdInfo(page.getLanguage().getNotation()), true, null);
                        monitor.worked(1);
                        ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                        WorkspaceOperations.openProcessDefinition((IFolder) definitionFile.getParent());
                        monitor.worked(1);
                        BasicNewResourceWizard.selectAndReveal(gpdFile, getActiveWorkbenchWindow());
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

    private IWorkbenchPage getActivePage() {
        return getActiveWorkbenchWindow().getActivePage();
    }

    private IWorkbenchWindow getActiveWorkbenchWindow() {
        return workbench.getActiveWorkbenchWindow();
    }

    private InputStream createInitialGpdInfo(String notation) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buffer.append("\n");
        buffer.append("\n");
        buffer.append("<process-diagram notation=\"").append(notation).append("\" showActions=\"true\"></process-diagram>");
        return new ByteArrayInputStream(buffer.toString().getBytes());
    }
}

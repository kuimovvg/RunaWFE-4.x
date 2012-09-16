package ru.runa.bpm.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.ProcessCache;
import ru.runa.bpm.ui.par.GpdXmlContentProvider;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.ProjectFinder;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;

public class NewProcessDefinitionWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;

    private IWorkbench workbench;

    private NewProcessDefinitionWizardPage page;

    public NewProcessDefinitionWizard() {
        setWindowTitle(Messages.getString("NewProcessDefinitionWizard.wizard.title"));
    }

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

                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        // TODO move to par provider
                        monitor.beginTask(Messages.getString("NewProcessDefinitionWizard.monitor.title"), 4);
                        IFolder folder = page.getProcessFolder();
                        folder.create(true, true, null);
                        monitor.worked(1);
                        IFile definitionFile = ProjectFinder.getProcessDefinitionFile(folder);
                        String processName = page.getProcessFolder().getName();
                        Document document = JpdlVersionRegistry.getContentProvider(page.getJpdlVersion()).getInitialProcessDefinitionDocument(
                                processName);
                        byte[] bytes = XmlUtil.writeXml(document);
                        definitionFile.create(new ByteArrayInputStream(bytes), true, null);
                        monitor.worked(1);
                        IFile gpdFile = folder.getFile(GpdXmlContentProvider.GPD_FILE_NAME);
                        gpdFile.create(createInitialGpdInfo(page.getNotation()), true, null);
                        monitor.worked(1);
                        ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                        IDE.openEditor(getActivePage(), definitionFile, true);
                        monitor.worked(1);
                        BasicNewResourceWizard.selectAndReveal(gpdFile, getActiveWorkbenchWindow());
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            DesignerLogger.logError(Messages.getString("NewProcessDefinitionWizard.error.creation"), e.getTargetException());
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

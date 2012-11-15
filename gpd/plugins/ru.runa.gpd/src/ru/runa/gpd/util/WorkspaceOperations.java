package ru.runa.gpd.util;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.editor.gef.GEFProcessEditor;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.ProcessSerializer;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ui.dialog.RenameProcessDefinitionDialog;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.gpd.ui.wizard.CopyProcessDefinitionWizard;
import ru.runa.gpd.ui.wizard.ExportParWizard;
import ru.runa.gpd.ui.wizard.ImportParWizard;
import ru.runa.gpd.ui.wizard.NewProcessDefinitionWizard;
import ru.runa.gpd.ui.wizard.NewProcessProjectWizard;

public class WorkspaceOperations {
    public static void deleteResources(List<IResource> resources) {
        List<IFile> deletedDefinitions = new ArrayList<IFile>();
        for (IResource resource : resources) {
            try {
                resource.refreshLocal(IResource.DEPTH_INFINITE, null);
                boolean processFolder = (resource instanceof IProject);
                String message = Localization.getString(processFolder ? "Delete.project.message" : "Delete.process.message");
                if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), Localization.getString("message.confirm.operation"),
                        MessageFormat.format(message, resource.getName()))) {
                    List<IFile> tmpFiles = new ArrayList<IFile>();
                    if (processFolder) {
                        for (IFile definitionFile : ProjectFinder.getProcessDefinitionFiles((IProject) resource)) {
                            tmpFiles.add(definitionFile);
                        }
                    } else {
                        tmpFiles.add(ProjectFinder.getProcessDefinitionFile((IFolder) resource));
                    }
                    resource.delete(true, null);
                    deletedDefinitions.addAll(tmpFiles);
                }
            } catch (CoreException e) {
                PluginLogger.logError("Error deleting", e);
            }
        }
        for (IFile definitionFile : deletedDefinitions) {
            ProcessCache.processDefinitionWasDeleted(definitionFile);
        }
    }

    public static void refreshResources(List<IResource> resources) {
        for (IResource resource : resources) {
            try {
                resource.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                PluginLogger.logError("Unable to refresh resource", e);
            }
        }
    }

    public static void createNewProject() {
        NewProcessProjectWizard wizard = new NewProcessProjectWizard();
        wizard.init(PlatformUI.getWorkbench(), null);
        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        dialog.open();
    }

    public static void createNewProcessDefinition(IStructuredSelection selection) {
        NewProcessDefinitionWizard wizard = new NewProcessDefinitionWizard();
        wizard.init(PlatformUI.getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        dialog.open();
    }

    public static void copyProcessDefinition(IStructuredSelection selection) {
        IFolder processDefinitionFolder = (IFolder) selection.getFirstElement();
        IDE.saveAllEditors(new IResource[] { processDefinitionFolder }, true);
        CopyProcessDefinitionWizard wizard = new CopyProcessDefinitionWizard();
        wizard.init(PlatformUI.getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        dialog.open();
    }

    public static void renameProcessDefinition(IStructuredSelection selection) {
        IFolder definitionFolder = (IFolder) selection.getFirstElement();
        IFile definitionFile = ProjectFinder.getProcessDefinitionFile(definitionFolder);
        RenameProcessDefinitionDialog dialog = new RenameProcessDefinitionDialog(definitionFolder);
        ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
        dialog.setName(definition.getName());
        if (dialog.open() == IDialogConstants.OK_ID) {
            String newName = dialog.getName();
            try {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IEditorPart editor = page.findEditor(new FileEditorInput(definitionFile));
                if (editor != null) {
                    page.closeEditor(editor, false);
                }
                IPath oldPath = definitionFolder.getFullPath();
                IPath newPath = definitionFolder.getParent().getFolder(new Path(newName)).getFullPath();
                definitionFolder.copy(newPath, true, null);
                ProcessCache.processDefinitionWasDeleted(definitionFile);
                definitionFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(newPath);
                IFile newDefinitionFile = ProjectFinder.getProcessDefinitionFile(definitionFolder);
                definition.setName(newName);
                saveProcessDefinition(newDefinitionFile, definition);
                ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                ResourcesPlugin.getWorkspace().getRoot().getFolder(oldPath).delete(true, null);
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

    public static void saveProcessDefinition(IFile definitionFile, ProcessDefinition definition) throws Exception {
        ProcessSerializer serializer = definition.getLanguage().getSerializer();
        Document document = serializer.getInitialProcessDefinitionDocument(definition.getName());
        serializer.saveToXML(definition, document);
        byte[] bytes = XmlUtil.writeXml(document);
        ParContentProvider.saveAuxInfo(definitionFile, definition);
        definitionFile.setContents(new ByteArrayInputStream(bytes), true, true, null);
    }

    public static void openProcessDefinition(IFolder definitionFolder) {
        try {
            IFile definitionFile = ProjectFinder.getProcessDefinitionFile(definitionFolder);
            ProcessDefinition processDefinition = ProcessCache.getProcessDefinition(definitionFile);
            String editorId;
            if (processDefinition.isBPMNNotation()) {
                editorId = GraphitiProcessEditor.ID;
            } else {
                editorId = GEFProcessEditor.ID;
            }
            IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), definitionFile, editorId, true);
        } catch (PartInitException e) {
            PluginLogger.logError("Unable open diagram", e);
        }
    }

    public static void exportProcessDefinition(IStructuredSelection selection) {
        ExportParWizard wizard = new ExportParWizard();
        wizard.init(PlatformUI.getWorkbench(), selection);
        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
        dialog.open();
    }

    public static void importProcessDefinition(IStructuredSelection selection) {
        ImportParWizard wizard = new ImportParWizard();
        wizard.init(PlatformUI.getWorkbench(), selection);
        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
        dialog.open();
    }
}

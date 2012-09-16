package ru.runa.bpm.ui.common.command;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.ParContentProvider;
import ru.runa.bpm.ui.ProcessCache;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.editor.DesignerEditor;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;

public class UpdateJpdlVersionCommand extends Command {
	private final IFile definitionFile;
	private final ProcessDefinition oldDefinition;
	private final DesignerEditor activeEditor;
	private boolean needsCloseEditorOnError = false;

	public UpdateJpdlVersionCommand(DesignerEditor activeEditor, IFile definitionFile, ProcessDefinition oldDefinition) {
		this.activeEditor = activeEditor;
		this.definitionFile = definitionFile;
		this.oldDefinition = oldDefinition;
	}
	
    @Override
    public void execute() {
        try {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            activePage.closeEditor(activeEditor, true);
            ProcessDefinition newDefinition = JpdlVersionRegistry.updateToNextVersion(oldDefinition, definitionFile);
			ParContentProvider.saveAuxInfo(definitionFile, newDefinition);
			ProcessCache.invalidateProcessDefinition(definitionFile);
			needsCloseEditorOnError = true;
			IDE.openEditor(activePage, new FileEditorInput(definitionFile), DesignerEditor.ID, true);
		} catch (Exception e) {
            DesignerLogger.logError(Messages.getString("Update.jpdl.error"), e);
            restorePreviousVersion();
		}
    }
    
    private void restorePreviousVersion() {
        try {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (needsCloseEditorOnError) {
                activePage.closeEditor(activePage.getActiveEditor(), true);
            }
            Document document = oldDefinition.getContentProvider().getInitialProcessDefinitionDocument(oldDefinition.getName());
            oldDefinition.getContentProvider().saveToXML(oldDefinition, document);
            byte[] bytes = XmlUtil.writeXml(document);
            definitionFile.setContents(new ByteArrayInputStream(bytes), true, true, null);
			ParContentProvider.saveAuxInfo(definitionFile, oldDefinition);
			ProcessCache.invalidateProcessDefinition(definitionFile);
			IDE.openEditor(activePage, new FileEditorInput(definitionFile), DesignerEditor.ID, true);
		} catch (Exception e) {
            DesignerLogger.logError("Undo update process definition to next jPDL version failed", e);
		}
    }
}

package ru.runa.gpd.lang.action;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.command.FormNodeSetScriptFileCommand;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.util.IOUtils;

public class OpenFormScriptDelegate extends BaseModelActionDelegate {
    private static final String EDITOR_ID = "tk.eclipse.plugin.jseditor.editors.JavaScriptEditor";

    @Override
    public void run(IAction action) {
        try {
            FormNode formNode = getSelection();
            String fileName;
            if (!formNode.hasFormScript()) {
                fileName = formNode.getNodeId() + "." + FormNode.SCRIPT_SUFFIX;
            } else {
                fileName = formNode.getScriptFileName();
            }
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
            if (!file.exists()) {
                file = IOUtils.createFileSafely(file);
                byte[] template = IOUtils.readStreamAsBytes(getClass().getResourceAsStream("/conf/form.template.js"));
                file.setContents(new ByteArrayInputStream(template), true, false, null);
                file.setCharset(PluginConstants.UTF_ENCODING, null);
            }
            if (!PluginConstants.UTF_ENCODING.equalsIgnoreCase(file.getCharset())) {
                // TODO bug for old processes fixed in ExportParWizard now
                file.setCharset(PluginConstants.UTF_ENCODING, null);
            }
            if (!formNode.hasFormScript()) {
                setNewScriptFormFile(formNode, file.getName());
            }
            IDE.openEditor(getWorkbenchPage(), file, EDITOR_ID, true);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private void setNewScriptFormFile(FormNode formNode, String fileName) {
        FormNodeSetScriptFileCommand command = new FormNodeSetScriptFileCommand();
        command.setFormNode(formNode);
        command.setScriptFileName(fileName);
        executeCommand(command);
    }
}

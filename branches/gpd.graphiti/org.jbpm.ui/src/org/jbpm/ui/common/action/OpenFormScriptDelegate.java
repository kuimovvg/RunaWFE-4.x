package org.jbpm.ui.common.action;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.common.command.FormNodeSetScriptFileCommand;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.common.part.graph.FormNodeEditPart;
import org.jbpm.ui.util.IOUtils;

public class OpenFormScriptDelegate extends BaseActionDelegate {
    private static final String EDITOR_ID = "tk.eclipse.plugin.jseditor.editors.JavaScriptEditor";
    
    public void run(IAction action) {
        try {
            FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();
            String fileName;
            if (!formNode.hasFormScript()) {
                fileName = formNode.getName() + "." + FormNode.SCRIPT_SUFFIX;
                fileName = IOUtils.fixFileName(fileName);
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
            IDE.openEditor(targetPart.getSite().getPage(), file, EDITOR_ID, true);
        } catch (Exception e) {
            DesignerLogger.logError(e);
        }
    }

    private void setNewScriptFormFile(FormNode formNode, String fileName) {
        FormNodeSetScriptFileCommand command = new FormNodeSetScriptFileCommand();
        command.setFormNode(formNode);
        command.setScriptFileName(fileName);
        executeCommand(command);
    }

}

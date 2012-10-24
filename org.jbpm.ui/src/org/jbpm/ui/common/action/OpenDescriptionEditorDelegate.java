package org.jbpm.ui.common.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.ParContentProvider;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.util.IOUtils;

public class OpenDescriptionEditorDelegate extends BaseActionDelegate {
    private static final String EDITOR_ID = "tk.eclipse.plugin.wysiwyg.WYSIWYGHTMLEditor";

    public void run(IAction action) {
        try {
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
            if (!file.exists()) {
                file = IOUtils.createFileSafely(file);
            }
            if (!PluginConstants.UTF_ENCODING.equalsIgnoreCase(file.getCharset())) {
                // TODO bug for imported processes fixed in ExportParWizard now
                file.setCharset(PluginConstants.UTF_ENCODING, null);
            }
            IDE.openEditor(targetPart.getSite().getPage(), file, EDITOR_ID);
        } catch (Exception e) {
            DesignerLogger.logError(e);
        }
    }

}

package ru.runa.bpm.ui.common.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.ParContentProvider;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.util.IOUtils;

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

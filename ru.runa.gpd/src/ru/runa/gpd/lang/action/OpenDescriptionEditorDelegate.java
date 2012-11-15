package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

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
            PluginLogger.logError(e);
        }
    }

}

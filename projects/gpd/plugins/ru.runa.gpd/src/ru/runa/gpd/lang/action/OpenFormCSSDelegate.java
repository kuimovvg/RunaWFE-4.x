package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class OpenFormCSSDelegate extends BaseModelActionDelegate {
    private static final String EDITOR_ID = "tk.eclipse.plugin.csseditor.editors.CSSEditor";

    @Override
    public void run(IAction action) {
        try {
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), ParContentProvider.FORM_CSS_FILE_NAME);
            if (!file.exists()) {
                file = IOUtils.createFileSafely(file);
            }
            IDE.openEditor(getWorkbenchPage(), file, EDITOR_ID, true);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }
}

package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class OpenFormCSSDelegate extends BaseActionDelegate {

    private static final String EDITOR_ID = "tk.eclipse.plugin.csseditor.editors.CSSEditor";

    public void run(IAction action) {
        try {
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), ParContentProvider.FORM_CSS_FILE_NAME);
            if (!file.exists()) {
                file = IOUtils.createFileSafely(file);
                file.setCharset(PluginConstants.UTF_ENCODING, null);
            }
            IDE.openEditor(targetPart.getSite().getPage(), file, EDITOR_ID, true);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

}

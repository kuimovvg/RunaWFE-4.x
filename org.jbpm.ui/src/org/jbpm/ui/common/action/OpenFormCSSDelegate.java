package org.jbpm.ui.common.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.ParContentProvider;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.util.IOUtils;

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
            DesignerLogger.logError(e);
        }
    }

}

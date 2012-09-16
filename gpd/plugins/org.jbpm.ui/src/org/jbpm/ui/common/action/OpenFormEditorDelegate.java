package ru.runa.bpm.ui.common.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.common.part.graph.FormNodeEditPart;
import ru.runa.bpm.ui.util.IOUtils;

public abstract class OpenFormEditorDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        try {
            FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();
            String fileName = formNode.getFormFileName();
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
            setPreferencesSafe(file);
            openInEditor(file, formNode);
        } catch (CoreException e) {
            DesignerLogger.logError(e);
            throw new RuntimeException(e);
        }
    }

    private void setPreferencesSafe(IFile file) throws CoreException {
        try {
            if (!PluginConstants.UTF_ENCODING.equalsIgnoreCase(file.getCharset())) {
                // TODO bug for imported processes fixed in ExportParWizard now
                file.setCharset(PluginConstants.UTF_ENCODING, null);
            }
        } catch (Exception e) {
        }
    }

    protected abstract void openInEditor(IFile file, FormNode formNode) throws CoreException;
}

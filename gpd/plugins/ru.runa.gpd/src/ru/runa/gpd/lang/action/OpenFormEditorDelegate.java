package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.gef.part.graph.FormNodeEditPart;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.util.IOUtils;

public abstract class OpenFormEditorDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        try {
            FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();
            String fileName = formNode.getFormFileName();
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
            setPreferencesSafe(file);
            openInEditor(file, formNode);
        } catch (CoreException e) {
            PluginLogger.logError(e);
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

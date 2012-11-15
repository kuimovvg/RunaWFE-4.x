package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import ru.runa.gpd.editor.gef.GEFProcessEditor;

public class SaveAll extends BaseActionDelegate {

    public void run(IAction action) {
        IEditorPart[] dirtyEditors = getDirtyEditors();
        for (IEditorPart editorPart : dirtyEditors) {
            if (!(editorPart instanceof GEFProcessEditor)) {
                editorPart.doSave(null);
            }
        }
        for (IEditorPart editorPart : dirtyEditors) {
            if (editorPart instanceof GEFProcessEditor) {
                editorPart.doSave(null);
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(getDirtyEditors().length > 0);
    }

    private IEditorPart[] getDirtyEditors() {
        return window.getActivePage().getDirtyEditors();
    }
}

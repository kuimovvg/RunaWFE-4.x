package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.bpm.ui.editor.DesignerEditor;

public class ShowActionsAction extends BaseActionDelegate {

    public void run(IAction action) {
		DesignerEditor editor = getActiveDesignerEditor();
        editor.getDefinition().setShowActions(!editor.getDefinition().isShowActions());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        DesignerEditor editor = getActiveDesignerEditor();
        action.setChecked(editor != null && editor.getDefinition().isShowActions());
        action.setEnabled(editor != null);
    }
}

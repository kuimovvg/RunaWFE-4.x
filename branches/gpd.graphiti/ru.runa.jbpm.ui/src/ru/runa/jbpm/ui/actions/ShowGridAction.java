package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.jbpm.ui.editor.DesignerEditor;

public class ShowGridAction extends BaseActionDelegate {

	public void run(IAction action) {
        DesignerEditor editor = getActiveDesignerEditor();
        editor.getDefinition().setShowGrid(!editor.getDefinition().isShowGrid());
	}

    @Override
	public void selectionChanged(IAction action, ISelection selection) {
        DesignerEditor editor = getActiveDesignerEditor();
        action.setChecked(editor != null && editor.getDefinition().isShowGrid());
        action.setEnabled(editor != null);
	}
}

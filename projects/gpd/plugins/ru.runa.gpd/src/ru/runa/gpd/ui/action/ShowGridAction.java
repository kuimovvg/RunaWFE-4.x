package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.editor.gef.GEFProcessEditor;

public class ShowGridAction extends BaseActionDelegate {

	public void run(IAction action) {
        GEFProcessEditor editor = getActiveDesignerEditor();
        editor.getDefinition().setShowGrid(!editor.getDefinition().isShowGrid());
	}

    @Override
	public void selectionChanged(IAction action, ISelection selection) {
        GEFProcessEditor editor = getActiveDesignerEditor();
        action.setChecked(editor != null && editor.getDefinition().isShowGrid());
        action.setEnabled(editor != null);
	}
}

package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.gef.GEFProcessEditor;

public class EnableAntialiasingAction extends BaseActionDelegate {

	public void run(IAction action) {
		Activator.getDefault().getDialogSettings().put(PluginConstants.DISABLE_ANTIALIASING, !action.isChecked());
		for (GEFProcessEditor editor : getOpenedDesignerEditors()) {
			editor.refresh();
		}
	}

    @Override
	public void selectionChanged(IAction action, ISelection selection) {
		action.setChecked(!Activator.getDefault().getDialogSettings().getBoolean(PluginConstants.DISABLE_ANTIALIASING));
	}
}

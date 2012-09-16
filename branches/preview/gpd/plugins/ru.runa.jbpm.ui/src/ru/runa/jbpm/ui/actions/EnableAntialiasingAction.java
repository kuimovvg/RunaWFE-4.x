package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.editor.DesignerEditor;

public class EnableAntialiasingAction extends BaseActionDelegate {

	public void run(IAction action) {
		DesignerPlugin.getDefault().getDialogSettings().put(PluginConstants.DISABLE_ANTIALIASING, !action.isChecked());
		for (DesignerEditor editor : getOpenedDesignerEditors()) {
			editor.refresh();
		}
	}

    @Override
	public void selectionChanged(IAction action, ISelection selection) {
		action.setChecked(!DesignerPlugin.getDefault().getDialogSettings().getBoolean(PluginConstants.DISABLE_ANTIALIASING));
	}
}

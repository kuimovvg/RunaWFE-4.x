package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.part.graph.FormNodeEditPart;
import ru.runa.gpd.lang.model.FormNode;

public class DeleteFormDelegate extends BaseActionDelegate {

	public void run(IAction action) {
		if (MessageDialog.openQuestion(
				targetPart.getSite().getShell(),
				Localization.getString("ConfirmDelete"), 
				Localization.getString("Form.WillBeDeleted"))) {
			FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();
			formNode.setFormFileName(FormNode.EMPTY);
			formNode.setValidationFileName(FormNode.EMPTY);
            formNode.setScriptFileName(FormNode.EMPTY);
		}
	}

}

package ru.runa.bpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.common.part.graph.FormNodeEditPart;
import ru.runa.bpm.ui.resource.Messages;

public class DeleteFormDelegate extends BaseActionDelegate {

	public void run(IAction action) {
		if (MessageDialog.openQuestion(
				targetPart.getSite().getShell(),
				Messages.getString("ConfirmDelete"), 
				Messages.getString("Form.WillBeDeleted"))) {
			FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();
			formNode.setFormFileName(FormNode.EMPTY);
			formNode.setValidationFileName(FormNode.EMPTY);
            formNode.setScriptFileName(FormNode.EMPTY);
		}
	}

}

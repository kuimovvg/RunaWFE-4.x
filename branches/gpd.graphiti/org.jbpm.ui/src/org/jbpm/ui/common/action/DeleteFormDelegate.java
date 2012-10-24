package org.jbpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.common.part.graph.FormNodeEditPart;
import org.jbpm.ui.resource.Messages;

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

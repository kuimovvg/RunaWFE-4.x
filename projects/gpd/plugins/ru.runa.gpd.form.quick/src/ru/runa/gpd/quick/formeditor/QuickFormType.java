package ru.runa.gpd.quick.formeditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.form.FormType;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;

public class QuickFormType extends FormType {

	@Override
	public IEditorPart openForm(IFile formFile, FormNode formNode)
			throws CoreException {
		return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, QuickFormEditor.ID, true);
	}

	@Override
	public Map<String, Integer> getFormVariableNames(IFile formFile,
			FormNode formNode) throws Exception {
        // TODO implement
		return new HashMap<String, Integer>();
	}

	@Override
	public void validate(IFile formFile, FormNode formNode, List<ValidationError> errors) {
		// TODO implement
	}

}

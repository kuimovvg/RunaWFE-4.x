package org.jbpm.ui.forms;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.dialog.InfoWithDetailsDialog;

public class FTLStubFormType extends FormType {

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        InfoWithDetailsDialog dialog = new InfoWithDetailsDialog("WARN", "Use external editor", "Restricted version; no required plugin 'tk.eclipse.plugin.wysiwyg'");
        dialog.open();
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFormFileName(IFile definitionFile, FormNode formNode) {
        return formNode.getName().concat(".").concat(formNode.getFormType());
    }

    @Override
    public Map<String, Integer> getFormVariableNames(IFile formFile, FormNode formNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate(IFile formFile, FormNode formNode) {
        // 
    }
}

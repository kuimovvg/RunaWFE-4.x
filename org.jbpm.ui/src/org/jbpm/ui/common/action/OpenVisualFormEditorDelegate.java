package org.jbpm.ui.common.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.forms.FormTypeProvider;

public class OpenVisualFormEditorDelegate extends OpenFormEditorDelegate {

    @Override
    protected void openInEditor(IFile file, FormNode formNode) throws CoreException {
        // open form
        FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode);
    }

}

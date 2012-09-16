package ru.runa.bpm.ui.common.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.forms.FormTypeProvider;

public class OpenVisualFormEditorDelegate extends OpenFormEditorDelegate {

    @Override
    protected void openInEditor(IFile file, FormNode formNode) throws CoreException {
        // open form
        FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode);
    }

}

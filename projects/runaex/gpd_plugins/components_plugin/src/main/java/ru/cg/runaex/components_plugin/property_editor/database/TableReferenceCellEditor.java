package ru.cg.runaex.components_plugin.property_editor.database;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import ru.cg.runaex.components.bean.component.part.TableReference;

public class TableReferenceCellEditor extends DialogCellEditor {

    public TableReferenceCellEditor(Composite parent) {
        super(parent, SWT.NONE);
    }

    @Override
    protected TableReference openDialogBox(Control cellEditorWindow) {
        IEditorPart actEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        IProject currentProject = null;
        if (actEditor != null) {
            IFile formFile = (IFile) (((IFileEditorInput) (actEditor.getEditorInput())).getFile());
            currentProject = formFile.getProject();
        }
        SelectTableReferenceDialog dialog = new SelectTableReferenceDialog(currentProject);
        dialog.open();
        TableReference ref = null;
        switch (dialog.getReturnCode()) {
        case Window.OK: {
            ref = dialog.getSelectedColumnReference();
            break;
        }
        case Window.CANCEL: {
            // Do nothing
            break;
        }
        }
        return ref;
    }

}
package ru.runa.gpd.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ru.runa.gpd.editor.ProcessEditorBase;

public abstract class BaseActionDelegate implements IWorkbenchWindowActionDelegate {
    protected IWorkbenchWindow window;

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    protected IEditorPart getActiveEditor() {
        return window.getActivePage().getActiveEditor();
    }

    protected List<ProcessEditorBase> getOpenedDesignerEditors() {
        List<ProcessEditorBase> editors = new ArrayList<ProcessEditorBase>();
        IEditorReference[] editorReferences = window.getActivePage().getEditorReferences();
        for (IEditorReference editorReference : editorReferences) {
            IEditorPart editor = editorReference.getEditor(true);
            if (editor instanceof ProcessEditorBase) {
                editors.add((ProcessEditorBase) editor);
            }
        }
        return editors;
    }

    protected ProcessEditorBase getActiveDesignerEditor() {
        IEditorPart editor = getActiveEditor();
        if (editor instanceof ProcessEditorBase) {
            return (ProcessEditorBase) editor;
        }
        return null;
    }

    protected IStructuredSelection getStructuredSelection() {
        ISelection selection = window.getSelectionService().getSelection();
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection) selection;
        }
        return null;
    }
}

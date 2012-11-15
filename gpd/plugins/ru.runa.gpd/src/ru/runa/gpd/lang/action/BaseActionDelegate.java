package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import ru.runa.gpd.editor.gef.GEFProcessEditor;
import ru.runa.gpd.editor.gef.OutlineViewer;

public abstract class BaseActionDelegate implements IObjectActionDelegate {

    protected IWorkbenchPart targetPart;

    protected EditPart selectedPart;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection == null || !(selection instanceof StructuredSelection))
            return;
        Object object = ((StructuredSelection) selection).getFirstElement();
        if (object instanceof EditPart) {
            selectedPart = (EditPart) object;
        }
    }

    protected void executeCommand(Command command) {
        CommandStack commandStack;
        if (targetPart instanceof GraphicalEditor) {
            commandStack = (CommandStack) ((GraphicalEditor) targetPart).getAdapter(CommandStack.class);
        } else {
            commandStack = ((OutlineViewer) ((ContentOutline) targetPart).getCurrentPage()).getCommandStack();
        }
        commandStack.execute(command);
    }
    
    protected IEditorPart getActiveEditor() {
        return targetPart.getSite().getPage().getActiveEditor();
    }

    protected GEFProcessEditor getActiveDesignerEditor() {
        IEditorPart editor = getActiveEditor();
        if (editor instanceof GEFProcessEditor) {
            return (GEFProcessEditor) editor;
        }
        return null;
    }

    protected IFile getDefinitionFile() {
        return getActiveDesignerEditor().getDefinitionFile();
    }

}

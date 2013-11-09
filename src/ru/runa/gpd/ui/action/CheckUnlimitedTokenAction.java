package ru.runa.gpd.ui.action;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.algorithms.CheckUnlimitedTokenAlgorithm;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;

public class CheckUnlimitedTokenAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        IEditorPart editorPart = getActiveEditor();
        if (editorPart != null) {
            IEditorInput editorInput = editorPart.getEditorInput();
            if (editorInput instanceof FileEditorInput) {
                ProcessDefinition definition = ProcessCache.getProcessDefinition(((FileEditorInput) editorInput).getFile());
                List<Transition> transitions = definition.getChildrenRecursive(Transition.class);
                List<Node> nodes = definition.getChildren(Node.class);
                CheckUnlimitedTokenAlgorithm algorithm = new CheckUnlimitedTokenAlgorithm(transitions, nodes);
                Transition redTransition = algorithm.startAlgorithm();
                if (redTransition != null) {
                    MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Localization.getString("message.warning"),
                            Localization.getString("CheckingTokensAction.SituationExist.Message", redTransition.getId()));
                } else {
                    MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Localization.getString("message.warning"),
                            Localization.getString("CheckingTokensAction.SituationNotExist.Message"));
                }
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        action.setEnabled(getDirtyEditors().length == 0 && editor != null && editor.getDefinition().getLanguage() == Language.BPMN
                && !editor.getDefinition().isInvalid());
    }

    private IEditorPart[] getDirtyEditors() {
        return window.getActivePage().getDirtyEditors();
    }
}

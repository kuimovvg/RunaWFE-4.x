package ru.runa.gpd.lang.action;

import java.util.List;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.TaskState;

public class EscalationOffAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        List<TaskState> states = editor.getDefinition().getChildren(TaskState.class);
        for (TaskState state : states) {
            state.setUseEscalation(false);
        }
    }
}

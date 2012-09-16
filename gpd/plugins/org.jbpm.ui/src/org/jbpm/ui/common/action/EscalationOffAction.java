package ru.runa.bpm.ui.common.action;

import java.util.List;

import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.editor.DesignerEditor;
import ru.runa.bpm.ui.jpdl3.model.TaskState;

public class EscalationOffAction extends BaseActionDelegate {

    public void run(IAction action) {
    	DesignerEditor editor = getActiveDesignerEditor();
    	List<TaskState> states = editor.getDefinition().getChildren(TaskState.class);
        for (TaskState state : states)
        	state.setUseEscalation(false);  	
    }

}

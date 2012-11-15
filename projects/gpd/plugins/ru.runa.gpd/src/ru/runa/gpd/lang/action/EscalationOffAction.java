package ru.runa.gpd.lang.action;

import java.util.List;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.editor.gef.GEFProcessEditor;
import ru.runa.gpd.lang.model.TaskState;

public class EscalationOffAction extends BaseActionDelegate {

    public void run(IAction action) {
    	GEFProcessEditor editor = getActiveDesignerEditor();
    	List<TaskState> states = editor.getDefinition().getChildren(TaskState.class);
        for (TaskState state : states)
        	state.setUseEscalation(false);  	
    }

}

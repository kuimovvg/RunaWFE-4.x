package org.jbpm.ui.common.action;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.jbpm.ui.editor.DesignerEditor;
import org.jbpm.ui.jpdl3.model.TaskState;

public class EscalationOffAction extends BaseActionDelegate {

    public void run(IAction action) {
    	DesignerEditor editor = getActiveDesignerEditor();
    	List<TaskState> states = editor.getDefinition().getChildren(TaskState.class);
        for (TaskState state : states)
        	state.setUseEscalation(false);  	
    }

}

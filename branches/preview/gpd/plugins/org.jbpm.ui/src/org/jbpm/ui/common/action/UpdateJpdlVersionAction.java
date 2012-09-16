package ru.runa.bpm.ui.common.action;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.common.command.UpdateJpdlVersionCommand;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.editor.DesignerEditor;

public class UpdateJpdlVersionAction extends BaseActionDelegate {

    public void run(IAction action) {
       	DesignerEditor editor = getActiveDesignerEditor();
       	Command command = new UpdateJpdlVersionCommand(editor, editor.getDefinitionFile(), editor.getDefinition());
       	executeCommand(command);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (selectedPart != null) {
            ProcessDefinition definition = (ProcessDefinition) selectedPart.getModel();
            action.setEnabled(JpdlVersionRegistry.canBeUpdatedToNextVersion(definition));
        } else {
            action.setEnabled(false);
        }
    }

}

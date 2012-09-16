package ru.runa.bpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.common.command.AddActionCommand;
import ru.runa.bpm.ui.common.model.Active;

public class AddActionDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        AddActionCommand command = new AddActionCommand();
        command.setTarget(getTargetElement());
        executeCommand(command);
    }

    protected Active getTargetElement() {
        return (Active) selectedPart.getModel();
    }

}

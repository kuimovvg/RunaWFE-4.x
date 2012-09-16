package ru.runa.bpm.ui.jpdl2.action;

import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.common.action.AddActionDelegate;
import ru.runa.bpm.ui.common.command.AddActionCommand;

public class AddActionDelegate2 extends AddActionDelegate {

    @Override
    public void run(IAction action) {
        AddActionCommand command = new AddActionCommand();
        command.setTarget(getTargetElement());
        executeCommand(command);
    }


}

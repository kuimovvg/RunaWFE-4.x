package ru.runa.bpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import ru.runa.bpm.ui.common.model.Bendpoint;
import ru.runa.bpm.ui.common.model.Transition;

public abstract class TransitionAbstractBendpointCommand extends Command {

    protected Transition transition;

    protected int index;

    protected Bendpoint bendpoint;

    public void setTransitionDecorator(Transition transition) {
        this.transition = transition;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setLocation(int x, int y) {
        bendpoint = new Bendpoint(x, y);
    }

}

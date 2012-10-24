package org.jbpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import org.jbpm.ui.common.model.Bendpoint;
import org.jbpm.ui.common.model.Transition;

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

package ru.runa.bpm.ui.common.command;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import ru.runa.bpm.ui.common.model.Node;

public class NodeChangeConstraintCommand extends Command {

    private Rectangle newConstraint;

    private Rectangle oldConstraint;

    private Node node;

    @Override
    public void execute() {
        oldConstraint = node.getConstraint();
        node.setConstraint(newConstraint);
    }

    @Override
    public void undo() {
        node.setConstraint(oldConstraint);
    }

    public void setNewConstraint(Rectangle r) {
        newConstraint = r;
    }

    public void setNode(Node node) {
        this.node = node;
    }

}

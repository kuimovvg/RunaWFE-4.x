package ru.runa.bpm.ui.common.command;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import ru.runa.bpm.ui.common.model.EndState;
import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.StartState;

public class NodeCreateCommand extends Command {

    private Node node;

    private Rectangle constraint;

    protected ProcessDefinition definition;

    @Override
    public void execute() {
        node.setConstraint(constraint);
        if (node.getName() == null) {
            node.setName(definition.getNextNodeName(node));
        }
        definition.addChild(node);
    }

    @Override
    public boolean canExecute() {
        if (node instanceof StartState) {
            return definition.getFirstChild(StartState.class) == null;
        }
        if (node instanceof EndState) {
            return definition.getFirstChild(EndState.class) == null;
        }
        return true;
    }

    @Override
    public void undo() {
        definition.removeChild(node);
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setConstraint(Rectangle constraint) {
        this.constraint = constraint;
    }

    public void setParent(ProcessDefinition parent) {
        this.definition = parent;
    }
}

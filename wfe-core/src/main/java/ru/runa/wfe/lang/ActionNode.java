package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;

import com.google.common.base.Preconditions;

public class ActionNode extends Node {
    private static final long serialVersionUID = 1L;
    private Action action;

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ActionNode;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(action, "action");
    }

    /**
     * override this method to customize the node behaviour.
     */
    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            // execute the action
            executeAction(action, executionContext);
            leave(executionContext);
        } catch (Exception exception) {
            // NOTE that Error's are not caught because that might halt the
            // JVM and mask the original Error.
            // search for an exception handler or throw to the client
            raiseException(exception);
        }
    }

}

package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;

/**
 * Used for embedded subprocess merging.
 * @since 4.1.0
 * @author dofs
 */
public class EmbeddedSubprocessEndNode extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.END_PROCESS;
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        leave(executionContext);
    }

}

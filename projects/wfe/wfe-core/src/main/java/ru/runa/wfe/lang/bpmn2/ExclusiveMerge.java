package ru.runa.wfe.lang.bpmn2;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

public class ExclusiveMerge extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.MERGE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        leave(executionContext, getDefaultLeavingTransitionNotNull());
    }

}

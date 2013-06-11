package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;

import com.google.common.base.Throwables;

public class Conjunction extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.MERGE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        try {
            log.info("Executing " + this); // TODO debug
            leave(executionContext);
        } catch (Exception e) {
            log.error("Failed " + this);
            throw Throwables.propagate(e);
        }
    }

}

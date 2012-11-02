package ru.runa.wfe.lang;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.execution.ExecutionContext;

public class WaitState extends Node {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(WaitState.class);

    @Override
    public NodeType getNodeType() {
        return NodeType.WaitState;
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        log.info("Waiting in " + this);
    }

}

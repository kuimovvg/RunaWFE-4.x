package ru.runa.wfe.lang;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.execution.ExecutionContext;

import com.google.common.base.Throwables;

public class Conjunction extends Node {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Conjunction.class);

    @Override
    public NodeType getNodeType() {
        return NodeType.Merge;
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

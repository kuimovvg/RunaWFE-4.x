package ru.runa.wfe.lang;

import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class ScriptTask extends Node {
    private static final long serialVersionUID = 1L;
    private Delegation delegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.ACTION_NODE;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(delegation, "delegation in " + this);
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        try {
            executionContext.addLog(new ActionLog(this));
            ActionHandler actionHandler = delegation.getInstance();
            log.info("Executing " + this); // TODO debug
            actionHandler.execute(executionContext);
            leave(executionContext);
        } catch (Exception e) {
            log.error("Failed " + this);
            throw Throwables.propagate(e);
        }
    }

}
